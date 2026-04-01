package com.garage;

import com.healthmarketscience.jackcess.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class CreateGarageDB {

    public static void main(String[] args) throws Exception {
        String outputPath = args.length > 0 ? args[0] : "GARAGE_2026.accdb";
        File dbFile = new File(outputPath);
        if (dbFile.exists()) dbFile.delete();

        System.out.println("Creation de la base : " + dbFile.getAbsolutePath());

        try (Database db = new DatabaseBuilder(dbFile)
                .setFileFormat(Database.FileFormat.V2016)
                .create()) {

            // ── Tables principales ──────────────────────────────────────
            Table client    = createClientTable(db);
            Table vehicule  = createVehiculeTable(db);
            Table ouvrier   = createOuvrierTable(db);
            Table operation = createOperationTable(db);
            Table visite    = createVisiteTable(db);
            Table demander  = createDemanderTable(db);
            Table realiser  = createRealiserTable(db);

            // ── Tables de sauvegarde (C Save = Création Sauvegarde) ─────
            // Structure = colonnes de la table source + [Date S] + [Heure S]
            Table savClient    = createCSaveTable(db, "C Save CLIENT",    client.getColumns());
            Table savDemander  = createCSaveTable(db, "C Save DEMANDER",  demander.getColumns());
            Table savOperation = createCSaveTable(db, "C Save OPERATION", operation.getColumns());
            Table savOuvrier   = createCSaveTable(db, "C Save OUVRIER",   ouvrier.getColumns());
            Table savRealiser  = createCSaveTable(db, "C Save REALISER",  realiser.getColumns());
            Table savVehicule  = createCSaveTable(db, "C Save VEHICULE",  vehicule.getColumns());
            Table savVisite    = createCSaveTable(db, "C Save VISITE",    visite.getColumns());

            // ── Insertion des données + sauvegarde initiale ─────────────
            LocalDate yesterday   = LocalDate.now().minusDays(1);
            LocalDateTime saveTime = LocalDateTime.now();

            insertClients(client,    savClient,    yesterday, saveTime);
            insertVehicules(vehicule, savVehicule,  yesterday, saveTime);
            insertOuvriers(ouvrier,   savOuvrier,   yesterday, saveTime);
            insertOperations(operation, savOperation, yesterday, saveTime);
            insertVisites(visite,    savVisite,    yesterday, saveTime);
            insertDemander(demander, savDemander,  yesterday, saveTime);
            insertRealiser(realiser, savRealiser,  yesterday, saveTime);

            System.out.println("Tables principales  : CLIENT, VEHICULE, OUVRIER, OPERATION, VISITE, DEMANDER, REALISER");
            System.out.println("Tables C Save       : C Save CLIENT/VEHICULE/OUVRIER/OPERATION/VISITE/DEMANDER/REALISER");
            System.out.println("  (champs Date S = hier, Heure S = maintenant)");
            System.out.println("Base GARAGE_2026.accdb creee avec succes !");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CREATION DES TABLES PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════

    static Table createClientTable(Database db) throws Exception {
        return new TableBuilder("CLIENT")
            .addColumn(new ColumnBuilder("NumClient",  DataType.LONG))
            .addColumn(new ColumnBuilder("Nom",        DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Prenom",     DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Adresse",    DataType.TEXT).setLength(100))
            .addColumn(new ColumnBuilder("CP",         DataType.TEXT).setLength(10))
            .addColumn(new ColumnBuilder("Ville",      DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Tel",        DataType.TEXT).setLength(20))
            .toTable(db);
    }

    static Table createVehiculeTable(Database db) throws Exception {
        return new TableBuilder("VEHICULE")
            .addColumn(new ColumnBuilder("Immatriculation", DataType.TEXT).setLength(20))
            .addColumn(new ColumnBuilder("Marque",          DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Modele",          DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("NumClient",       DataType.LONG))
            .toTable(db);
    }

    static Table createOuvrierTable(Database db) throws Exception {
        return new TableBuilder("OUVRIER")
            .addColumn(new ColumnBuilder("NumOuvrier",    DataType.LONG))
            .addColumn(new ColumnBuilder("NomOuvrier",    DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("PrenomOuvrier", DataType.TEXT).setLength(50))
            .toTable(db);
    }

    static Table createOperationTable(Database db) throws Exception {
        return new TableBuilder("OPERATION")
            .addColumn(new ColumnBuilder("CodeOpe",     DataType.TEXT).setLength(10))
            .addColumn(new ColumnBuilder("LibOpe",      DataType.TEXT).setLength(100))
            .addColumn(new ColumnBuilder("CoutHoraire", DataType.DOUBLE))
            .toTable(db);
    }

    static Table createVisiteTable(Database db) throws Exception {
        return new TableBuilder("VISITE")
            .addColumn(new ColumnBuilder("NumVisite",       DataType.LONG))
            .addColumn(new ColumnBuilder("DateVisite",      DataType.SHORT_DATE_TIME))
            .addColumn(new ColumnBuilder("Immatriculation", DataType.TEXT).setLength(20))
            .addColumn(new ColumnBuilder("KM",              DataType.LONG))
            .toTable(db);
    }

    static Table createDemanderTable(Database db) throws Exception {
        return new TableBuilder("DEMANDER")
            .addColumn(new ColumnBuilder("NumVisite", DataType.LONG))
            .addColumn(new ColumnBuilder("CodeOpe",   DataType.TEXT).setLength(10))
            .toTable(db);
    }

    static Table createRealiserTable(Database db) throws Exception {
        return new TableBuilder("REALISER")
            .addColumn(new ColumnBuilder("NumVisite",  DataType.LONG))
            .addColumn(new ColumnBuilder("CodeOpe",    DataType.TEXT).setLength(10))
            .addColumn(new ColumnBuilder("NumOuvrier", DataType.LONG))
            .addColumn(new ColumnBuilder("Duree",      DataType.DOUBLE))
            .toTable(db);
    }

    /**
     * Crée une table C Save avec les mêmes colonnes que la table source
     * + [Date S] (date) et [Heure S] (date/heure).
     */
    static Table createCSaveTable(Database db, String name,
                                   List<? extends Column> srcCols) throws Exception {
        TableBuilder tb = new TableBuilder(name);
        for (Column c : srcCols) {
            ColumnBuilder cb = new ColumnBuilder(c.getName(), c.getType());
            if (c.getType() == DataType.TEXT) cb.setLength(c.getLength());
            tb.addColumn(cb);
        }
        tb.addColumn(new ColumnBuilder("Date S",  DataType.SHORT_DATE_TIME));
        tb.addColumn(new ColumnBuilder("Heure S", DataType.SHORT_DATE_TIME));
        return tb.toTable(db);
    }

    // ═══════════════════════════════════════════════════════════════════
    // INSERTION DES DONNEES
    // Chaque méthode insère dans la table principale ET dans C Save
    // (la C Save reçoit en plus Date S = hier, Heure S = maintenant)
    // ═══════════════════════════════════════════════════════════════════

    static void insertClients(Table t, Table save,
                               LocalDate dateS, LocalDateTime heureS) throws Exception {
        Object[][] data = {
            {1, "DUPONT",   "Jean",    "12 rue de la Paix",       "75001", "Paris",      "0612345678"},
            {2, "MARTIN",   "Marie",   "5 avenue Foch",           "69001", "Lyon",       "0698765432"},
            {3, "BERNARD",  "Pierre",  "8 boulevard Victor Hugo", "13001", "Marseille",  "0611223344"},
            {4, "LEROY",    "Sophie",  "23 rue des Lilas",        "75002", "Paris",      "0755443322"},
            {5, "MOREAU",   "Paul",    "15 rue du Commerce",      "31000", "Toulouse",   "0644332211"},
            {6, "SIMON",    "Claire",  "3 impasse des Roses",     "33000", "Bordeaux",   "0677889900"},
            {7, "LAURENT",  "Nicolas", "47 avenue Jean Jaures",   "59000", "Lille",      "0623456789"},
            {8, "THOMAS",   "Emma",    "9 rue du Moulin",         "67000", "Strasbourg", "0688990011"},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    static void insertVehicules(Table t, Table save,
                                 LocalDate dateS, LocalDateTime heureS) throws Exception {
        Object[][] data = {
            {"AB-123-CD", "Peugeot",    "308",    1},
            {"EF-456-GH", "Renault",    "Clio",   2},
            {"IJ-789-KL", "Citroen",    "C3",     3},
            {"MN-012-OP", "Volkswagen", "Golf",   4},
            {"QR-345-ST", "Toyota",     "Yaris",  5},
            {"UV-678-WX", "Peugeot",    "208",    1},  // Client 1 - 2 véhicules
            {"YZ-901-AB", "Ford",       "Focus",  6},
            {"CD-234-EF", "Renault",    "Megane", 7},
            {"GH-567-IJ", "BMW",        "Serie3", 8},
            {"KL-890-MN", "Citroen",    "C5",     3},  // Client 3 - 2 véhicules
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    static void insertOuvriers(Table t, Table save,
                                LocalDate dateS, LocalDateTime heureS) throws Exception {
        Object[][] data = {
            {1, "GARCIA",   "Miguel"},
            {2, "PETIT",    "Thomas"},
            {3, "ROUSSEAU", "Antoine"},
            {4, "LAMBERT",  "Lucas"},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    static void insertOperations(Table t, Table save,
                                  LocalDate dateS, LocalDateTime heureS) throws Exception {
        Object[][] data = {
            {"OPE01", "Vidange",                    80.0},
            {"OPE02", "Revision complete",           90.0},
            {"OPE03", "Changement plaquettes frein", 70.0},
            {"OPE04", "Changement pneus",            60.0},
            {"OPE05", "Diagnostic electronique",    100.0},
            {"OPE06", "Remplacement batterie",       75.0},
            {"OPE07", "Climatisation",               85.0},
            {"OPE08", "Remplacement courroie",       95.0},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    static void insertVisites(Table t, Table save,
                               LocalDate dateS, LocalDateTime heureS) throws Exception {
        // 35 visites – visites 23 et 32 prêtes pour tester la facture TTC
        // Plusieurs visites par immatriculation pour Req 4-6-1 (doublons)
        Object[][] data = {
            { 1, LocalDate.of(2024,  1,  8), "AB-123-CD", 32000},
            { 2, LocalDate.of(2024,  1, 15), "EF-456-GH", 18500},
            { 3, LocalDate.of(2024,  1, 22), "IJ-789-KL", 55000},
            { 4, LocalDate.of(2024,  2,  5), "MN-012-OP", 27000},
            { 5, LocalDate.of(2024,  2, 12), "QR-345-ST", 41000},
            { 6, LocalDate.of(2024,  2, 19), "UV-678-WX", 12000},
            { 7, LocalDate.of(2024,  3,  4), "YZ-901-AB", 63000},
            { 8, LocalDate.of(2024,  3, 11), "AB-123-CD", 33500},  // doublon
            { 9, LocalDate.of(2024,  3, 18), "CD-234-EF", 22000},
            {10, LocalDate.of(2024,  4,  2), "GH-567-IJ", 48000},
            {11, LocalDate.of(2024,  4,  9), "KL-890-MN", 71000},
            {12, LocalDate.of(2024,  4, 16), "EF-456-GH", 19800},  // doublon
            {13, LocalDate.of(2024,  5,  7), "MN-012-OP", 28500},  // doublon
            {14, LocalDate.of(2024,  5, 14), "QR-345-ST", 43000},  // doublon
            {15, LocalDate.of(2024,  5, 21), "AB-123-CD", 35000},  // doublon
            {16, LocalDate.of(2024,  6,  3), "IJ-789-KL", 57000},  // doublon
            {17, LocalDate.of(2024,  6, 10), "UV-678-WX", 14000},  // doublon
            {18, LocalDate.of(2024,  6, 17), "YZ-901-AB", 65000},  // doublon
            {19, LocalDate.of(2024,  7,  1), "CD-234-EF", 24000},  // doublon
            {20, LocalDate.of(2024,  7,  8), "GH-567-IJ", 50000},  // doublon
            {21, LocalDate.of(2024,  7, 22), "KL-890-MN", 73000},  // doublon
            {22, LocalDate.of(2024,  8,  5), "MN-012-OP", 30000},  // doublon
            // ── Visite 23 : test facture ────────────────────────────────
            // OPE01 (4×¼h × 80€ = 80€HT) + OPE02 (8×¼h × 90€ = 180€HT)
            // Total : 260€HT → 312€TTC
            {23, LocalDate.of(2024,  8, 19), "AB-123-CD", 36500},  // doublon
            {24, LocalDate.of(2024,  9,  2), "EF-456-GH", 21000},  // doublon
            {25, LocalDate.of(2024,  9,  9), "QR-345-ST", 44500},  // doublon
            {26, LocalDate.of(2024,  9, 16), "UV-678-WX", 16000},  // doublon
            {27, LocalDate.of(2024, 10,  7), "YZ-901-AB", 67000},  // doublon
            {28, LocalDate.of(2024, 10, 14), "IJ-789-KL", 59000},  // doublon
            {29, LocalDate.of(2024, 10, 21), "GH-567-IJ", 52000},  // doublon
            {30, LocalDate.of(2024, 11,  4), "CD-234-EF", 26000},  // doublon
            {31, LocalDate.of(2024, 11, 18), "KL-890-MN", 75000},  // doublon
            // ── Visite 32 : test facture ────────────────────────────────
            // OPE03 (6×¼h × 70€ = 105€HT) + OPE05 (2×¼h × 100€ = 50€HT)
            // Total : 155€HT → 186€TTC
            {32, LocalDate.of(2024, 12,  2), "EF-456-GH", 22500},  // doublon
            {33, LocalDate.of(2024, 12,  9), "AB-123-CD", 38000},  // doublon
            {34, LocalDate.of(2024, 12, 16), "MN-012-OP", 31500},  // doublon
            {35, LocalDate.of(2025,  1,  6), "QR-345-ST", 46000},  // doublon
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    static void insertDemander(Table t, Table save,
                                LocalDate dateS, LocalDateTime heureS) throws Exception {
        Object[][] data = {
            { 1,"OPE01"},{ 1,"OPE02"},
            { 2,"OPE03"},{ 2,"OPE04"},
            { 3,"OPE05"},
            { 4,"OPE01"},{ 4,"OPE06"},
            { 5,"OPE02"},{ 5,"OPE07"},
            { 6,"OPE08"},
            { 7,"OPE01"},
            { 8,"OPE03"},{ 8,"OPE05"},
            { 9,"OPE02"},
            {10,"OPE04"},{10,"OPE06"},
            {11,"OPE01"},
            {12,"OPE07"},{12,"OPE08"},
            {13,"OPE01"},{13,"OPE02"},
            {14,"OPE03"},
            {15,"OPE05"},{15,"OPE06"},
            {16,"OPE01"},
            {17,"OPE04"},
            {18,"OPE02"},{18,"OPE07"},
            {19,"OPE08"},
            {20,"OPE01"},{20,"OPE03"},
            {21,"OPE05"},
            {22,"OPE06"},
            {23,"OPE01"},{23,"OPE02"},  // visite 23
            {24,"OPE04"},
            {25,"OPE07"},
            {26,"OPE01"},{26,"OPE08"},
            {27,"OPE02"},
            {28,"OPE03"},
            {29,"OPE05"},{29,"OPE06"},
            {30,"OPE01"},
            {31,"OPE04"},
            {32,"OPE03"},{32,"OPE05"},  // visite 32
            {33,"OPE07"},
            {34,"OPE01"},{34,"OPE02"},
            {35,"OPE08"},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    static void insertRealiser(Table t, Table save,
                                LocalDate dateS, LocalDateTime heureS) throws Exception {
        Object[][] data = {
            { 1,"OPE01",1, 4.0},{ 1,"OPE02",2, 8.0},
            { 2,"OPE03",1, 6.0},{ 2,"OPE04",3, 8.0},
            { 3,"OPE05",2, 2.0},
            { 4,"OPE01",1, 4.0},{ 4,"OPE06",4, 3.0},
            { 5,"OPE02",2,10.0},{ 5,"OPE07",3, 6.0},
            { 6,"OPE08",1,12.0},
            { 7,"OPE01",2, 4.0},
            { 8,"OPE03",3, 6.0},{ 8,"OPE05",1, 2.0},
            { 9,"OPE02",2, 8.0},
            {10,"OPE04",4, 8.0},{10,"OPE06",1, 3.0},
            {11,"OPE01",3, 4.0},
            {12,"OPE07",2, 6.0},{12,"OPE08",4,12.0},
            {13,"OPE01",1, 4.0},{13,"OPE02",3, 8.0},
            {14,"OPE03",2, 6.0},
            {15,"OPE05",1, 2.0},{15,"OPE06",4, 3.0},
            {16,"OPE01",3, 4.0},
            {17,"OPE04",2, 8.0},
            {18,"OPE02",1,10.0},{18,"OPE07",4, 6.0},
            {19,"OPE08",3,12.0},
            {20,"OPE01",2, 4.0},{20,"OPE03",1, 6.0},
            {21,"OPE05",4, 2.0},
            {22,"OPE06",3, 3.0},
            // Visite 23 : 260€ HT → 312€ TTC
            {23,"OPE01",1, 4.0},  // 4×¼h × 80€ =  80€ HT
            {23,"OPE02",2, 8.0},  // 8×¼h × 90€ = 180€ HT
            {24,"OPE04",3, 8.0},
            {25,"OPE07",1, 6.0},
            {26,"OPE01",2, 4.0},{26,"OPE08",4,12.0},
            {27,"OPE02",3, 8.0},
            {28,"OPE03",1, 6.0},
            {29,"OPE05",2, 2.0},{29,"OPE06",4, 3.0},
            {30,"OPE01",3, 4.0},
            {31,"OPE04",1, 8.0},
            // Visite 32 : 155€ HT → 186€ TTC
            {32,"OPE03",1, 6.0},  // 6×¼h × 70€ = 105€ HT
            {32,"OPE05",3, 2.0},  // 2×¼h ×100€ =  50€ HT
            {33,"OPE07",2, 6.0},
            {34,"OPE01",4, 4.0},{34,"OPE02",1, 8.0},
            {35,"OPE08",3,12.0},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(appendSave(row, dateS, heureS));
        }
    }

    /** Ajoute Date S et Heure S en fin de ligne pour les tables C Save. */
    static Object[] appendSave(Object[] row, LocalDate dateS, LocalDateTime heureS) {
        Object[] saveRow = Arrays.copyOf(row, row.length + 2);
        saveRow[row.length]     = dateS;
        saveRow[row.length + 1] = heureS;
        return saveRow;
    }
}
