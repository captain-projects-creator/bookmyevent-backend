/*package com.example.event_booking.util;

// import net.glxn.qrgen.javase.QRCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class QRCodeGenerator {
    public static String generateQRCodeImage(String text) throws Exception {
        File dir = new File("qrcodes");
        if (!dir.exists()) dir.mkdirs();

        /* String filePath = "qrcodes/" + text.replace(" ", "_") + ".png";
        try (OutputStream out = new FileOutputStream(filePath)) {
            QRCode.from(text).withSize(250, 250).writeTo(out);
        }
        return filePath;
    }
}
*/