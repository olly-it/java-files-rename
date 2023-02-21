package it.olly.filesrename;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

class FileDateTest {
    // sdf for macOs Touch
    final SimpleDateFormat sdfTouch = new SimpleDateFormat("yyyyMMddHHmm");
    final SimpleDateFormat sdfSetfile = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    @Test
    void displayDates() throws IOException {
        // directory where files are
        File dir = new File("/Users/olly/_/gopro-data-sbagliata");
        File[] files = dir.listFiles();

        for (File f : files) {
            Path path = Paths.get(f.getAbsolutePath());
            System.out.println("[" + f.getAbsolutePath() + "] has [" + Files.getAttribute(path, "creationTime") + "]");
        }

    }

    @Test
    void modifyDates() throws Exception {
        // directory where files are
        File dir = new File("/Users/olly/_/gopro-data-sbagliata");
        File[] files = dir.listFiles();

        for (File f : files) {
            // + 8 years, -2 months, -10 days, -2 hours, -30 minutes
            Times times = calculateNewTime(f.getAbsolutePath(), 8, -2, -10, -2, -30);
            // modifyCreationDate(f.getAbsolutePath(), times.newCreationDate);
            modifyCreationDateMacOs(f.getAbsolutePath(), times.newCreationDate);
            System.out.println("[" + f.getAbsolutePath() + "] from [" + times.oldCreationDate + "] to ["
                    + times.newCreationDate + "]");
        }
    }

    record Times(FileTime oldCreationDate, FileTime newCreationDate) {
    }

    public Times calculateNewTime(String filePath, int plusYears, int plusMonths, int plusDays, int plusHours,
            int plusMinutes) {
        Path path = Paths.get(filePath);
        // get file time
        FileTime creationTime = null;
        try {
            creationTime = (FileTime) Files.getAttribute(path, "creationTime");
        } catch (IOException ex) {
        }

        // compose new creation date
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(creationTime.toMillis());
        date.add(Calendar.YEAR, plusYears);
        date.add(Calendar.MONTH, plusMonths);
        date.add(Calendar.DAY_OF_MONTH, plusDays);
        date.add(Calendar.HOUR_OF_DAY, plusHours);
        date.add(Calendar.MINUTE, plusMinutes);

        // set new creation date
        FileTime timeToSet = FileTime.fromMillis(date.getTimeInMillis());
        return new Times(creationTime, timeToSet);
    }

    public void modifyCreationDateMacOs(String filePath, FileTime timeToSet) throws Exception {
        // with setfile - ok but it does strange things on other date fields
        {
            String dt = sdfSetfile.format(new Date(timeToSet.toMillis()));
            String[] exec = new String[] { "setfile", "-d", dt, filePath };
            Process process = (new ProcessBuilder(exec)).start();
            process.waitFor();
        }

        /* run also touch now
         NOTE: It only allows the creation date to be changed to something earlier than the current modification date */
        {
            String dt = sdfTouch.format(new Date(timeToSet.toMillis()));
            String[] exec = new String[] { "touch", "-t", dt, filePath };
            Process process = (new ProcessBuilder(exec)).start();
            process.waitFor();
        }
    }

    public void modifyCreationDate(String filePath, FileTime timeToSet) throws Exception {
        Path path = Paths.get(filePath);

        // BasicFileAttributeView attributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
        // attributes.setTimes(timeToSet, timeToSet, timeToSet);

        Files.setAttribute(path, "lastAccessTime", timeToSet);
        Files.setAttribute(path, "lastModifiedTime", timeToSet);
        Files.setAttribute(path, "creationTime", timeToSet);

    }

}
