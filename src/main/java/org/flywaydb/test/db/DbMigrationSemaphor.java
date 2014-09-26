package org.flywaydb.test.db;

public class DbMigrationSemaphor {
    private static DbMigrationSemaphor dbMigrationSemaphor = new DbMigrationSemaphor();

    private int desiredNumber;
    private int currentNumber = 0;

    public static DbMigrationSemaphor dbMigrationSemaphor() {
        return dbMigrationSemaphor;
    }

    public synchronized void registerReadyForMigration() {
        currentNumber++;
        System.out.println(currentNumber + " .vs " + desiredNumber);
    }

    public boolean isReadyToMigrate() {
        return desiredNumber == currentNumber;
    }

    public void reset() {
        currentNumber = 0;
    }

    public void setDesiredNumber(int desiredNumber) {
        this.desiredNumber = desiredNumber;
    }
}
