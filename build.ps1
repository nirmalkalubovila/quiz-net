# QuizNet Build and Run Script for Windows PowerShell

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   QuizNet Build & Run Script" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Function to show menu
function Show-Menu {
    Write-Host "Select an option:" -ForegroundColor Yellow
    Write-Host "1. Build All (Server + Client)" -ForegroundColor White
    Write-Host "2. Build Server Only" -ForegroundColor White
    Write-Host "3. Build Client Only" -ForegroundColor White
    Write-Host "4. Run Enhanced Server (WebSocket)" -ForegroundColor Green
    Write-Host "5. Run Original Server" -ForegroundColor Green
    Write-Host "6. Run Console Client" -ForegroundColor Green
    Write-Host "7. Open Web Client" -ForegroundColor Green
    Write-Host "8. Clean Build" -ForegroundColor Red
    Write-Host "9. Full Setup (Build + Run Server + Open Web)" -ForegroundColor Magenta
    Write-Host "0. Exit" -ForegroundColor White
    Write-Host ""
}

# Function to build server
function Build-Server {
    Write-Host "`nBuilding server..." -ForegroundColor Yellow
    
    if (Test-Path "out/server") {
        Remove-Item -Recurse -Force "out/server" -ErrorAction SilentlyContinue
    }
    
    javac -d out/server server/*.java
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Server built successfully!" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ Server build failed!" -ForegroundColor Red
        return $false
    }
}

# Function to build client
function Build-Client {
    Write-Host "`nBuilding client..." -ForegroundColor Yellow
    
    if (Test-Path "out/client") {
        Remove-Item -Recurse -Force "out/client" -ErrorAction SilentlyContinue
    }
    
    javac -d out/client client/*.java
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Client built successfully!" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ Client build failed!" -ForegroundColor Red
        return $false
    }
}

# Function to run enhanced server
function Run-EnhancedServer {
    Write-Host "`nStarting Enhanced QuizServer (WebSocket support)..." -ForegroundColor Yellow
    Write-Host "Server will listen on port 9000" -ForegroundColor Cyan
    Write-Host "Press Ctrl+C to stop the server`n" -ForegroundColor Gray
    
    java -cp out/server EnhancedQuizServer
}

# Function to run original server
function Run-OriginalServer {
    Write-Host "`nStarting Original QuizServer..." -ForegroundColor Yellow
    Write-Host "Server will listen on port 9000" -ForegroundColor Cyan
    Write-Host "Press Ctrl+C to stop the server`n" -ForegroundColor Gray
    
    java -cp out/server QuizServer
}

# Function to run client
function Run-Client {
    Write-Host "`nStarting Console Client..." -ForegroundColor Yellow
    Write-Host "Connecting to localhost:9000`n" -ForegroundColor Cyan
    
    java -cp out/client QuizClient
}

# Function to open web client
function Open-WebClient {
    Write-Host "`nOpening web client..." -ForegroundColor Yellow
    
    $webPath = Join-Path $PSScriptRoot "web\index.html"
    
    if (Test-Path $webPath) {
        Start-Process $webPath
        Write-Host "✓ Web client opened in default browser!" -ForegroundColor Green
        Write-Host "`nMake sure the Enhanced Server is running!" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Web client not found at: $webPath" -ForegroundColor Red
    }
}

# Function to clean build
function Clean-Build {
    Write-Host "`nCleaning build artifacts..." -ForegroundColor Yellow
    
    if (Test-Path "out") {
        Remove-Item -Recurse -Force "out" -ErrorAction SilentlyContinue
        Write-Host "✓ Build cleaned!" -ForegroundColor Green
    } else {
        Write-Host "Nothing to clean." -ForegroundColor Gray
    }
}

# Function for full setup
function Full-Setup {
    Write-Host "`n========== FULL SETUP ==========" -ForegroundColor Magenta
    
    # Build
    if (Build-Server) {
        if (Build-Client) {
            Write-Host "`n✓ Build completed successfully!" -ForegroundColor Green
            
            # Start server in background
            Write-Host "`nStarting server in background..." -ForegroundColor Yellow
            Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp out/server EnhancedQuizServer"
            
            Start-Sleep -Seconds 2
            
            # Open web client
            Open-WebClient
            
            Write-Host "`n✓ Setup complete! You can now play QuizNet!" -ForegroundColor Green
            Write-Host "The server is running in a separate window." -ForegroundColor Cyan
        }
    }
}

# Main loop
do {
    Show-Menu
    $choice = Read-Host "Enter your choice"
    
    switch ($choice) {
        "1" {
            Build-Server
            Build-Client
        }
        "2" {
            Build-Server
        }
        "3" {
            Build-Client
        }
        "4" {
            if (Test-Path "out/server") {
                Run-EnhancedServer
            } else {
                Write-Host "Server not built. Please build first (option 1 or 2)." -ForegroundColor Red
            }
        }
        "5" {
            if (Test-Path "out/server") {
                Run-OriginalServer
            } else {
                Write-Host "Server not built. Please build first (option 1 or 2)." -ForegroundColor Red
            }
        }
        "6" {
            if (Test-Path "out/client") {
                Run-Client
            } else {
                Write-Host "Client not built. Please build first (option 1 or 3)." -ForegroundColor Red
            }
        }
        "7" {
            Open-WebClient
        }
        "8" {
            Clean-Build
        }
        "9" {
            Full-Setup
        }
        "0" {
            Write-Host "`nGoodbye! Thanks for using QuizNet!" -ForegroundColor Cyan
            exit
        }
        default {
            Write-Host "`nInvalid option. Please try again." -ForegroundColor Red
        }
    }
    
    if ($choice -ne "0" -and $choice -ne "4" -and $choice -ne "5" -and $choice -ne "6" -and $choice -ne "9") {
        Write-Host "`nPress any key to continue..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        Clear-Host
    }
    
} while ($choice -ne "0")
