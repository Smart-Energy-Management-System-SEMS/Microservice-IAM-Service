param(
    [Parameter(Mandatory=$false)]
    [string]$Url = "https://your-render-service.onrender.com/",

    [Parameter(Mandatory=$false)]
    [int]$IntervalSeconds = 600,

    [Parameter(Mandatory=$false)]
    [int]$TimeoutSeconds = 15
)

if ($IntervalSeconds -lt 30) {
    Write-Error "IntervalSeconds debe ser >= 30 para evitar rate limit o bloqueo del proveedor."
    exit 1
}

Write-Host "[keep-alive] Iniciando ping periodico"
Write-Host "[keep-alive] URL: $Url"
Write-Host "[keep-alive] Intervalo: $IntervalSeconds segundos"
Write-Host "[keep-alive] Presiona Ctrl + C para detener"

while ($true) {
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec $TimeoutSeconds -UseBasicParsing
        Write-Host "[$timestamp] OK - Status $($response.StatusCode)"
    }
    catch {
        Write-Host "[$timestamp] ERROR - $($_.Exception.Message)"
    }

    Start-Sleep -Seconds $IntervalSeconds
}
