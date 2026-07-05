# Attributi e Metodi delle Classi Boundary (Frontend Angular)

Questo documento raggruppa le classi Boundary per macro-aree, associandole agli oggetti Control ed Entity come definito nel documento ODD e nel codice sorgente. Per ciascuna Boundary, sono esplicitati i relativi **attributi di stato** e i **metodi** implementati.

---

## 1. Gestione Account (Autenticazione e Registrazione)
- **Oggetti Control:** `AuthControl`, `RegistrationControl`
- **Modello Oggetti (Entity):** `UtenteAfam`, `Token`

| Classe Boundary | Attributi (Tipo) | Metodi |
|---|---|---|
| **LoginBoundary** | `loginForm` (FormGroup)<br>`errorMessage` (string) | `onSubmit()`<br>`onGuestLogin()` |
| **RegistrationBoundary** | `registerForm` (FormGroup)<br>`errorMessage` (string)<br>`successMessage` (string)<br>`comuni` (any[])<br>`comuniFiltrati` (any[]) | `ngOnInit()`<br>`filtraComuni(val: string)`<br>`onSubmit()`<br>`generaCF()` |
| **OtpBoundary** | `otpForm` (FormGroup)<br>`errorMessage` (string)<br>`username` (string) | `ngOnInit()`<br>`onSubmit()` |
| **IdentityProviderBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |
| **RecoveryBoundary** | `email` (string)<br>`username` (string)<br>`mode` ('password' \| 'email')<br>`successMessage` (string)<br>`errorMessage` (string) | `switchMode(newMode: 'password' \| 'email')`<br>`requestPasswordRecovery()`<br>`requestEmailRecovery()` |
| **RecoveryResetBoundary** | `token` (string)<br>`newPassword` (any)<br>`confirmPassword` (any)<br>`errorMessage` (string)<br>`successMessage` (string) | `ngOnInit()`<br>`resetPassword()` |

---

## 2. Gestione Profilo
- **Oggetti Control:** `ProfileControl`
- **Modello Oggetti (Entity):** `Profilo`

| Classe Boundary | Attributi (Tipo) | Metodi |
|---|---|---|
| **ProfileBoundary** | `setupStep` ('idle' \| 'awaiting_otp' \| 'success')<br>`otpCode` (string)<br>`errorMessage` (string)<br>`profilo` (any) | `ngOnInit()`<br>`onSetup2fa()`<br>`onConfirm2fa()` |
| **EditAnagraficaBoundary** | `anagrafica` (any: {nome, cognome, dataNascita, codiceFiscale, citta, indirizzo, telefono})<br>`errorMessage` (string)<br>`successMessage` (string) | `ngOnInit()`<br>`save()` |
| **EditCredentialsBoundary** | `credentials` (any: {username, email})<br>`errorMessage` (string)<br>`successMessage` (string) | `ngOnInit()`<br>`save()` |
| **EditPasswordBoundary** | `passwords` (any: {currentPassword, newPassword, confirmPassword})<br>`errorMessage` (string)<br>`successMessage` (string) | `save()` |
| **OtherEditsBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |
| **PublicProfileBoundary** | `profilo` (any)<br>`errorMessage` (string) | `ngOnInit()`<br>`loadProfile(id: number)` |

---

## 3. Caricamento e Gestione Contenuti
- **Oggetti Control:** `UploadControl`, `ContentControl`, `ViewControl`
- **Modello Oggetti (Entity):** `Contenuto`, `Allegato`, `Gruppo`

| Classe Boundary | Attributi (Tipo) | Metodi |
|---|---|---|
| **ContentManagementBoundary** | `contents` (any[])<br>`errorMessage` (string)<br>`successMessage` (string)<br>`selectedContentId` (number \| null)<br>`scadenzaGiorni` (number)<br>`shareModalInstance` (any)<br>`groups` (any[])<br>`selectedGroupId` (number \| null)<br>`groupModalInstance` (any) | `ngOnInit()`<br>`loadContents()`<br>`download(allegatoId: number, filename: string)`<br>`delete(id: number)`<br>`share(contenutoId: number)`<br>`openGroupModal(contentId: number)`<br>`closeGroupModal()`<br>`confirmGroupAggregation()` |
| **UploadContentBoundary** | `uploadForm` (FormGroup)<br>`selectedFile` (File \| null)<br>`selectedDescrizioneFile` (File \| null)<br>`errorMessage` (string)<br>`successMessage` (string) | `onFileSelected(event: any)`<br>`onDescrizioneFileSelected(event: any)`<br>`onSubmit()` |
| **ContentViewBoundary** | `contenuto` (any)<br>`errorMessage` (string) | `ngOnInit()`<br>`loadContent(id: number)`<br>`download()` |
| **PreviewBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |
| **GroupManagementBoundary** | `groups` (any[])<br>`newGroupName` (string)<br>`errorMessage` (string)<br>`successMessage` (string) | `ngOnInit()`<br>`loadGroups()`<br>`createGroup()`<br>`deleteGroup(id: number)` |
| **GroupViewBoundary** | `group` (any)<br>`errorMessage` (string) | `ngOnInit()`<br>`loadGroup(id: number)` |
| **PublicContentViewBoundary** | `contenuto` (any)<br>`errorMessage` (string) | `ngOnInit()`<br>`loadContent(id: number)`<br>`downloadAllegato(allegatoId: number, nomeFile: string)` |

---

## 4. Condivisione
- **Oggetti Control:** `ShareControl`
- **Modello Oggetti (Entity):** `Link`

| Classe Boundary | Attributi (Tipo) | Metodi |
|---|---|---|
| **ShareBoundary** | `contenuto` (any)<br>`errorMessage` (string)<br>`identificatore` (string) | `ngOnInit()`<br>`loadSharedContent()`<br>`download()` |
| **ActiveLinksBoundary** | `links` (any[])<br>`errorMessage` (string) | `ngOnInit()`<br>`loadLinks()`<br>`copyLink(identificatore: string)`<br>`revoke(identificatore: string)` |
| **LinkSettingsBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |

---

## 5. Ricerca e Visualizzazione
- **Oggetti Control:** `SearchControl`, `ViewControl`
- **Modello Oggetti (Entity):** `Profilo`, `Contenuto`, `Gruppo` (Tutti i dati recuperabili)

| Classe Boundary | Attributi (Tipo) | Metodi |
|---|---|---|
| **ResultsBoundary** | `query` (string)<br>`utenti` (any[])<br>`contenuti` (any[])<br>`gruppi` (any[])<br>`errorMessage` (string)<br>`isSearching` (boolean) | `ngOnInit()`<br>`performSearch()` |
| **HomeBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |

---

## 6. Gestione Errori e Messaggistica
- **Oggetti Control:** *(Gestione errori tramite status HTTP dei vari Control)*
- **Modello Oggetti (Entity):** *(Nessuna entità di base dati diretta, opera mediante messaggistica HTTP/DTO)*

| Classe Boundary | Attributi (Tipo) | Metodi |
|---|---|---|
| **ErrorMessageBoundary** | `errorType` (string)<br>`errorMessage` (string)<br>`errorIcon` (string) | `ngOnInit()`<br>`setupErrorDetails()` |
| **MessageBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |
| **ErrorPageBoundary** | *(Nessun attributo di stato esplicito)* | *(Nessun metodo esplicito)* |

> **Nota:** I costruttori (es. `constructor(private router: Router...)`) non sono elencati nella colonna dei metodi poiché utilizzati esclusivamente per l'iniezione delle dipendenze dei servizi Angular, coerentemente con l'esclusione delle proprietà dei servizi.
