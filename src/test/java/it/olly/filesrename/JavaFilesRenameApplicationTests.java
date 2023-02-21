package it.olly.filesrename;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class JavaFilesRenameApplicationTests {

    @Test
    void renameTest() {
        // search everything like DSC_(number).JPG
        Pattern p = Pattern.compile("DSC_(\\d*).JPG");
        // directory where files are
        File dir = new File("/Users/olly/_/festa_rebe_maschera_18-02-2023/2/");
        File[] files = dir.listFiles();

        for (File f : files) {
            Matcher m = p.matcher(f.getName());
            if (m.find()) {
                String number = m.group(1);
                // rename file to DSC_2_(number).JPG
                f.renameTo(new File(dir, "DSC_2_" + number + ".JPG"));
            }
        }
    }

}
