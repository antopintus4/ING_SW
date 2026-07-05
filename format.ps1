$wdReplaceAll = 2
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    $doc = $word.Documents.Open("c:\Users\ANTO\Desktop\Ingegneria_del_software\Proj\RAD_NUOVO.docx")
    
    # Rimuovi i commenti
    while ($doc.Comments.Count -gt 0) {
        $doc.Comments.Item(1).Delete()
    }
    
    # Accetta le revisioni
    $doc.Revisions.AcceptAll()

    # Sostituzioni testo
    $find = $doc.Content.Find
    $find.ClearFormatting()
    $find.Replacement.ClearFormatting()

    $find.Execute("Visualizza_contenuto", $false, $false, $false, $false, $false, $true, 1, $false, "Visualizza contenuto", 2)
    $find.Execute("Visibilita'", $false, $false, $false, $false, $false, $true, 1, $false, "Visibilità", 2)
    $find.Execute("7.1.3.1.3. ", $false, $false, $false, $false, $false, $true, 1, $false, "- ", 2)
    $find.Execute("7.1.3.2.3. ", $false, $false, $false, $false, $false, $true, 1, $false, "- ", 2)
    $find.Execute("7.1.4.3 Il sistema", $false, $false, $false, $false, $false, $true, 1, $false, "- Il sistema", 2)
    $find.Execute("6.1.1.6.5. ", $false, $false, $false, $false, $false, $true, 1, $false, "- ", 2)
    $find.Execute("6.1.1.6.4. ", $false, $false, $false, $false, $false, $true, 1, $false, "- ", 2)
    $find.Execute("7.1.1.7.3. ", $false, $false, $false, $false, $false, $true, 1, $false, "- ", 2)
    $find.Execute("6.1.2.4 Il sistema", $false, $false, $false, $false, $false, $true, 1, $false, "- Il sistema", 2)

    $doc.Save()
    $doc.Close()
    Write-Output "Modifiche applicate con successo"
} catch {
    Write-Output "Errore: $_"
} finally {
    $word.Quit()
}
