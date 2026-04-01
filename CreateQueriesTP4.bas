Attribute VB_Name = "CreateQueriesTP4"
' ================================================================
' MODULE : CreateQueriesTP4
' USAGE  : Dans Access, ouvrir l'editeur VBA (Alt+F11)
'          Importer ce fichier : Fichier > Importer un fichier
'          Puis executer la macro : F5 sur Sub CreateAllQueries()
' ================================================================
' Ce module cree automatiquement toutes les requetes du TP4.
' Les requetes existantes du meme nom sont ecrasees.
' ================================================================

Option Compare Database
Option Explicit

' ----------------------------------------------------------------
' POINT D'ENTREE PRINCIPAL
' ----------------------------------------------------------------
Public Sub CreateAllQueries()
    On Error GoTo ErrHandler

    DoCmd.SetWarnings False

    ' ── [4.1] C Save (Requetes Creation = Make Table) ───────────
    Call MakeQuery("Req 4-1-1 : C Save CLIENT", _
        "SELECT CLIENT.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save CLIENT] " & _
        "FROM CLIENT;")

    Call MakeQuery("Req 4-1-2 : C Save DEMANDER", _
        "SELECT DEMANDER.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save DEMANDER] " & _
        "FROM DEMANDER;")

    Call MakeQuery("Req 4-1-3 : C Save OPERATION", _
        "SELECT OPERATION.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save OPERATION] " & _
        "FROM OPERATION;")

    Call MakeQuery("Req 4-1-4 : C Save OUVRIER", _
        "SELECT OUVRIER.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save OUVRIER] " & _
        "FROM OUVRIER;")

    Call MakeQuery("Req 4-1-5 : C Save REALISER", _
        "SELECT REALISER.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save REALISER] " & _
        "FROM REALISER;")

    Call MakeQuery("Req 4-1-6 : C Save VEHICULE", _
        "SELECT VEHICULE.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save VEHICULE] " & _
        "FROM VEHICULE;")

    Call MakeQuery("Req 4-1-7 : C Save VISITE", _
        "SELECT VISITE.*, DateAdd('d',-1,Date()) AS [Date S], Now() AS [Heure S] " & _
        "INTO [C Save VISITE] " & _
        "FROM VISITE;")

    ' ── [4.2] Save (Requetes Ajout = Append) ────────────────────
    Call MakeQuery("Req 4-2-1 : Save CLIENT", _
        "INSERT INTO [C Save CLIENT] " & _
        "SELECT CLIENT.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM CLIENT;")

    Call MakeQuery("Req 4-2-2 : Save DEMANDER", _
        "INSERT INTO [C Save DEMANDER] " & _
        "SELECT DEMANDER.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM DEMANDER;")

    Call MakeQuery("Req 4-2-3 : Save OPERATION", _
        "INSERT INTO [C Save OPERATION] " & _
        "SELECT OPERATION.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM OPERATION;")

    Call MakeQuery("Req 4-2-4 : Save OUVRIER", _
        "INSERT INTO [C Save OUVRIER] " & _
        "SELECT OUVRIER.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM OUVRIER;")

    Call MakeQuery("Req 4-2-5 : Save REALISER", _
        "INSERT INTO [C Save REALISER] " & _
        "SELECT REALISER.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM REALISER;")

    Call MakeQuery("Req 4-2-6 : Save VEHICULE", _
        "INSERT INTO [C Save VEHICULE] " & _
        "SELECT VEHICULE.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM VEHICULE;")

    Call MakeQuery("Req 4-2-7 : Save VISITE", _
        "INSERT INTO [C Save VISITE] " & _
        "SELECT VISITE.*, Date() AS [Date S], Now() AS [Heure S] " & _
        "FROM VISITE;")

    ' ── [4.3] Mise a jour telephone ─────────────────────────────
    ' Req 4-3-1 : probleme = le parametre [Nouveau N° Tel] apparait
    ' avant le critere car il est ecrit en 1er dans le SQL.
    Call MakeQuery("Req 4-3-1 : MAJ N de tel du Client", _
        "UPDATE CLIENT " & _
        "SET Tel = [Nouveau N de tel :] " & _
        "WHERE NumClient = [N du Client :];")

    ' Req 4-3-2 : solution en 2 requetes pour forcer l'ordre des saisies.
    ' 4-3-2a : on demande d'abord le N° de client (saisie 1)
    Call MakeQuery("Req 4-3-2a : SEL N du Client", _
        "SELECT NumClient, Nom, Prenom, Tel " & _
        "FROM CLIENT " & _
        "WHERE NumClient = [Saisir le N du Client :];")

    ' 4-3-2b : reference [Req 4-3-2a] → Access pose d'abord le N° client
    ' (pour evaluer la sous-requete), puis demande le nouveau tel (saisie 2).
    Call MakeQuery("Req 4-3-2b : MAJ N tel Client", _
        "UPDATE CLIENT " & _
        "SET Tel = [Nouveau N de tel :] " & _
        "WHERE NumClient IN " & _
        "  (SELECT NumClient FROM [Req 4-3-2a : SEL N du Client]);")

    ' ── [4.4] Suppression / Restauration ────────────────────────
    ' RAZ : supprime TOUS les enregistrements de DEMANDER
    Call MakeQuery("Req 4-4-1 : RAZ DEMANDER", _
        "DELETE * FROM DEMANDER;")

    ' Restauration : reinjecte depuis C Save DEMANDER
    Call MakeQuery("Req 4-4-2 : Restauration DEMANDER (Initialisation)", _
        "INSERT INTO DEMANDER ( NumVisite, CodeOpe ) " & _
        "SELECT [C Save DEMANDER].NumVisite, [C Save DEMANDER].CodeOpe " & _
        "FROM [C Save DEMANDER];")

    ' Suppression avec critere sur le CP du client
    Call MakeQuery("Req 4-4-3 : Sup DEMANDE erronne (en fonction de la CP)", _
        "DELETE DEMANDER.* " & _
        "FROM CLIENT " & _
        "  INNER JOIN (VEHICULE " & _
        "    INNER JOIN (VISITE " & _
        "      INNER JOIN DEMANDER " & _
        "        ON VISITE.NumVisite = DEMANDER.NumVisite) " & _
        "    ON VEHICULE.Immatriculation = VISITE.Immatriculation) " & _
        "  ON CLIENT.NumClient = VEHICULE.NumClient " & _
        "WHERE CLIENT.CP = [Saisir le code postal a supprimer :];")

    ' ── [4.4] Restauration de toutes les tables (facultatif) ────
    Call MakeQuery("Req 4-4-21 : Restauration CLIENT", _
        "INSERT INTO CLIENT ( NumClient, Nom, Prenom, Adresse, CP, Ville, Tel ) " & _
        "SELECT NumClient, Nom, Prenom, Adresse, CP, Ville, Tel " & _
        "FROM [C Save CLIENT];")

    Call MakeQuery("Req 4-4-22 : Restauration VEHICULE", _
        "INSERT INTO VEHICULE ( Immatriculation, Marque, Modele, NumClient ) " & _
        "SELECT Immatriculation, Marque, Modele, NumClient " & _
        "FROM [C Save VEHICULE];")

    Call MakeQuery("Req 4-4-23 : Restauration OPERATION", _
        "INSERT INTO OPERATION ( CodeOpe, LibOpe, CoutHoraire ) " & _
        "SELECT CodeOpe, LibOpe, CoutHoraire " & _
        "FROM [C Save OPERATION];")

    Call MakeQuery("Req 4-4-24 : Restauration OUVRIER", _
        "INSERT INTO OUVRIER ( NumOuvrier, NomOuvrier, PrenomOuvrier ) " & _
        "SELECT NumOuvrier, NomOuvrier, PrenomOuvrier " & _
        "FROM [C Save OUVRIER];")

    Call MakeQuery("Req 4-4-25 : Restauration VISITE", _
        "INSERT INTO VISITE ( NumVisite, DateVisite, Immatriculation, KM ) " & _
        "SELECT NumVisite, DateVisite, Immatriculation, KM " & _
        "FROM [C Save VISITE];")

    Call MakeQuery("Req 4-4-26 : Restauration REALISER", _
        "INSERT INTO REALISER ( NumVisite, CodeOpe, NumOuvrier, Duree ) " & _
        "SELECT NumVisite, CodeOpe, NumOuvrier, Duree " & _
        "FROM [C Save REALISER];")

    ' ── [4.5] Calcul de la facture TTC ──────────────────────────
    ' Rappel : Duree est en 1/4 d'heure, TVA = 20%
    ' Tester avec NumVisite = 23  (resultat : 260€ HT → 312€ TTC)
    '          ou NumVisite = 32  (resultat : 155€ HT → 186€ TTC)

    ' 4-5-1a : detail ligne par ligne
    Call MakeQuery("Req 4-5-1a : Detail Calcul de la facture TTC d une visite", _
        "SELECT V.NumVisite, V.DateVisite, " & _
        "       O.LibOpe, " & _
        "       O.CoutHoraire, " & _
        "       R.Duree AS [Duree (1/4h)], " & _
        "       R.Duree/4 AS [Duree (h)], " & _
        "       (R.Duree/4)*O.CoutHoraire AS [Montant HT], " & _
        "       (R.Duree/4)*O.CoutHoraire*1.2 AS [Montant TTC] " & _
        "FROM (VISITE AS V " & _
        "  INNER JOIN REALISER AS R ON V.NumVisite = R.NumVisite) " & _
        "  INNER JOIN OPERATION AS O ON R.CodeOpe = O.CodeOpe " & _
        "WHERE V.NumVisite = [Numero de visite :];")

    ' 4-5-1b : total de la visite
    Call MakeQuery("Req 4-5-1b : Calcul de la facture TTC", _
        "SELECT V.NumVisite, V.DateVisite, " & _
        "       Sum((R.Duree/4)*O.CoutHoraire)     AS [Total HT], " & _
        "       Sum((R.Duree/4)*O.CoutHoraire)*1.2 AS [Total TTC] " & _
        "FROM (VISITE AS V " & _
        "  INNER JOIN REALISER AS R ON V.NumVisite = R.NumVisite) " & _
        "  INNER JOIN OPERATION AS O ON R.CodeOpe = O.CodeOpe " & _
        "WHERE V.NumVisite = [Numero de visite :] " & _
        "GROUP BY V.NumVisite, V.DateVisite;")

    ' ── [4.6] Doublons sur immatriculation ──────────────────────
    ' Version simple : immatriculations avec plus d'une visite
    Call MakeQuery("Req 4-6-1 : Multi Visite (Doublons sur immatriculation)", _
        "SELECT V.Immatriculation, Count(*) AS [Nb Visites] " & _
        "FROM VISITE AS V " & _
        "GROUP BY V.Immatriculation " & _
        "HAVING Count(*) > 1;")

    ' Version enrichie : ajoute Nom client, Marque, Modele + tri par date
    Call MakeQuery("Req 4-6-1b : Multi Visite avec client et vehicule", _
        "SELECT V.DateVisite, V.NumVisite, V.Immatriculation, " & _
        "       VE.Marque, VE.Modele, " & _
        "       C.Nom, C.Prenom " & _
        "FROM CLIENT AS C " & _
        "  INNER JOIN (VEHICULE AS VE " & _
        "    INNER JOIN VISITE AS V " & _
        "      ON VE.Immatriculation = V.Immatriculation) " & _
        "  ON C.NumClient = VE.NumClient " & _
        "WHERE V.Immatriculation IN ( " & _
        "    SELECT Immatriculation FROM VISITE " & _
        "    GROUP BY Immatriculation HAVING Count(*) > 1) " & _
        "ORDER BY V.DateVisite;")

    DoCmd.SetWarnings True
    MsgBox "Toutes les requetes du TP4 ont ete creees avec succes !" & vbCrLf & _
           "(" & CurrentDb.QueryDefs.Count & " requetes au total)", _
           vbInformation, "TP4 - Requetes creees"
    Exit Sub

ErrHandler:
    DoCmd.SetWarnings True
    MsgBox "Erreur lors de la creation des requetes :" & vbCrLf & _
           Err.Number & " - " & Err.Description, _
           vbCritical, "Erreur"
End Sub

' ----------------------------------------------------------------
' HELPER : cree ou remplace une requete
' ----------------------------------------------------------------
Private Sub MakeQuery(sName As String, sSQL As String)
    Dim db As DAO.Database
    Set db = CurrentDb

    ' Supprimer si elle existe deja
    On Error Resume Next
    db.QueryDefs.Delete sName
    On Error GoTo 0

    ' Creer la nouvelle requete
    db.CreateQueryDef sName, sSQL
    Debug.Print "OK : " & sName
End Sub
