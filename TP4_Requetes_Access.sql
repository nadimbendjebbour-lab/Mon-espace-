-- ============================================================
-- TP N°4 - GARAGE 2026 - Requêtes Access
-- ============================================================
-- Structure supposée des tables principales :
--   CLIENT      (NumClient, Nom, Prenom, Adresse, CP, Ville, Tel)
--   VEHICULE    (Immatriculation, Marque, Modele, NumClient)
--   VISITE      (NumVisite, DateVisite, Immatriculation, KM)
--   OPERATION   (CodeOpe, LibOpe, CoutHoraire)
--   DEMANDER    (NumVisite, CodeOpe)          -- opérations demandées
--   REALISER    (NumVisite, CodeOpe, NumOuvrier, Duree)  -- durée en 1/4 heure
--   OUVRIER     (NumOuvrier, NomOuvrier, PrenomOuvrier)
--   [C Save DEMANDER]  (copie de sauvegarde de DEMANDER)
-- ============================================================


-- ============================================================
-- [4.4] SUPPRESSION ET RESTAURATION D'ENREGISTREMENT(S)
-- ============================================================

-- ------------------------------------------------------------
-- Req 4-4-1 : RAZ DEMANDER
-- Supprime TOUS les enregistrements de la table DEMANDER
-- ------------------------------------------------------------
DELETE *
FROM DEMANDER;


-- ------------------------------------------------------------
-- Req 4-4-2 : Restauration DEMANDER (Initialisation)
-- Restaure la table DEMANDER à partir de sa sauvegarde
-- Prérequis : la table DEMANDER est vide (après Req 4-4-1)
-- ------------------------------------------------------------
INSERT INTO DEMANDER ( NumVisite, CodeOpe )
SELECT [C Save DEMANDER].NumVisite, [C Save DEMANDER].CodeOpe
FROM [C Save DEMANDER];


-- ------------------------------------------------------------
-- Req 4-4-3 : Sup DEMANDE erroné (en fonction de la CP)
-- Supprime les enregistrements de DEMANDER dont le client
-- habite dans un code postal précis (paramètre saisi)
-- Jointures : DEMANDER → VISITE → VEHICULE → CLIENT
-- ------------------------------------------------------------
DELETE DEMANDER.*
FROM CLIENT
  INNER JOIN (VEHICULE
    INNER JOIN (VISITE
      INNER JOIN DEMANDER
        ON VISITE.NumVisite = DEMANDER.NumVisite)
    ON VEHICULE.Immatriculation = VISITE.Immatriculation)
  ON CLIENT.NumClient = VEHICULE.NumClient
WHERE CLIENT.CP = [Saisir le code postal à supprimer :];


-- ============================================================
-- [4.5] CALCUL DE FACTURE TTC D'UNE VISITE
-- Rappel : Duree en 1/4 heure, TVA = 20 %
-- Pour tester : utiliser NumVisite = 23 ou 32
-- ============================================================

-- ------------------------------------------------------------
-- Req 4-5-1a : Détail Calcul de la facture TTC d'une visite
-- Affiche le détail ligne par ligne (une ligne par opération)
-- ------------------------------------------------------------
SELECT
    V.NumVisite,
    V.DateVisite,
    O.LibOpe,
    O.CoutHoraire,
    R.Duree                                      AS [Durée (1/4h)],
    R.Duree / 4                                  AS [Durée (h)],
    (R.Duree / 4) * O.CoutHoraire                AS [Montant HT],
    (R.Duree / 4) * O.CoutHoraire * 1.2          AS [Montant TTC]
FROM (VISITE AS V
  INNER JOIN REALISER AS R
    ON V.NumVisite = R.NumVisite)
  INNER JOIN OPERATION AS O
    ON R.CodeOpe = O.CodeOpe
WHERE V.NumVisite = [Numéro de visite :];


-- ------------------------------------------------------------
-- Req 4-5-1b : Calcul de la facture TTC (total de la visite)
-- ------------------------------------------------------------
SELECT
    V.NumVisite,
    V.DateVisite,
    Sum((R.Duree / 4) * O.CoutHoraire)       AS [Total HT],
    Sum((R.Duree / 4) * O.CoutHoraire) * 1.2 AS [Total TTC]
FROM (VISITE AS V
  INNER JOIN REALISER AS R
    ON V.NumVisite = R.NumVisite)
  INNER JOIN OPERATION AS O
    ON R.CodeOpe = O.CodeOpe
WHERE V.NumVisite = [Numéro de visite :]
GROUP BY V.NumVisite, V.DateVisite;


-- ============================================================
-- [4.6] VÉHICULES RÉPARÉS PLUSIEURS FOIS (DOUBLONS)
-- ============================================================

-- ------------------------------------------------------------
-- Req 4-6-1 : Multi Visite (Doublons sur immatriculation)
-- Trouve les immatriculations présentes plus d'une fois
-- dans VISITE (requête type "Recherche des doublons")
-- ------------------------------------------------------------
SELECT V.Immatriculation, Count(*) AS [Nb Visites]
FROM VISITE AS V
GROUP BY V.Immatriculation
HAVING Count(*) > 1;


-- ------------------------------------------------------------
-- Req 4-6-1 (version enrichie) :
-- Ajoute Nom client, Marque, Modèle + tri par date de visite
-- ------------------------------------------------------------
SELECT
    V.DateVisite,
    V.NumVisite,
    V.Immatriculation,
    VE.Marque,
    VE.Modele,
    C.Nom,
    C.Prenom
FROM CLIENT AS C
  INNER JOIN (VEHICULE AS VE
    INNER JOIN VISITE AS V
      ON VE.Immatriculation = V.Immatriculation)
  ON C.NumClient = VE.NumClient
WHERE V.Immatriculation IN (
    SELECT Immatriculation
    FROM VISITE
    GROUP BY Immatriculation
    HAVING Count(*) > 1
)
ORDER BY V.DateVisite;


-- ============================================================
-- [FACULTATIF] RESTAURATION DE TOUTES LES TABLES
-- ============================================================

-- ------------------------------------------------------------
-- Req 4-4-21 : Restauration CLIENT
-- ------------------------------------------------------------
DELETE * FROM CLIENT;

INSERT INTO CLIENT (NumClient, Nom, Prenom, Adresse, CP, Ville, Tel)
SELECT NumClient, Nom, Prenom, Adresse, CP, Ville, Tel
FROM [C Save CLIENT];

-- ------------------------------------------------------------
-- Req 4-4-22 : Restauration VEHICULE
-- ------------------------------------------------------------
DELETE * FROM VEHICULE;

INSERT INTO VEHICULE (Immatriculation, Marque, Modele, NumClient)
SELECT Immatriculation, Marque, Modele, NumClient
FROM [C Save VEHICULE];

-- ------------------------------------------------------------
-- Req 4-4-23 : Restauration OPERATION
-- ------------------------------------------------------------
DELETE * FROM OPERATION;

INSERT INTO OPERATION (CodeOpe, LibOpe, CoutHoraire)
SELECT CodeOpe, LibOpe, CoutHoraire
FROM [C Save OPERATION];

-- ------------------------------------------------------------
-- Req 4-4-24 : Restauration OUVRIER
-- ------------------------------------------------------------
DELETE * FROM OUVRIER;

INSERT INTO OUVRIER (NumOuvrier, NomOuvrier, PrenomOuvrier)
SELECT NumOuvrier, NomOuvrier, PrenomOuvrier
FROM [C Save OUVRIER];

-- ------------------------------------------------------------
-- Req 4-4-25 : Restauration VISITE
-- ------------------------------------------------------------
DELETE * FROM VISITE;

INSERT INTO VISITE (NumVisite, DateVisite, Immatriculation, KM)
SELECT NumVisite, DateVisite, Immatriculation, KM
FROM [C Save VISITE];

-- ------------------------------------------------------------
-- Req 4-4-26 : Restauration REALISER
-- ------------------------------------------------------------
DELETE * FROM REALISER;

INSERT INTO REALISER (NumVisite, CodeOpe, NumOuvrier, Duree)
SELECT NumVisite, CodeOpe, NumOuvrier, Duree
FROM [C Save REALISER];

-- ============================================================
-- FIN DU FICHIER
-- ============================================================
