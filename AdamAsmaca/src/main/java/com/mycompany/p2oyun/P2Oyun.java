/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.p2oyun;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.Timer;
/**
 *
 * @author Musti
 */

public class P2Oyun extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(P2Oyun.class.getName());
    
    private final String BASE_PATH = "C:\\P2Oyun";
    private final String TXT_PATH = BASE_PATH + "\\TXTDosyalar";
    private final String IMG_PATH = BASE_PATH + "\\Resimler";

    private String secilenKelime = "";
    private int hataliGirisSayisi = 0;
    private int oyunSuresi = 0;
    private Timer saniyeTimer;
    private int sifreHataSayisi = 0;
    private boolean oyunDevamEdiyor = false;
    
    public P2Oyun() {
        initComponents();
        kelimePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));
        baslangicKontrolleri();
        tablolariGuncelle();
        // oyunuBaslat(); kendimiz başlatıcaz
    }
    
    public void baslangicKontrolleri() {
        File txtDir = new File(TXT_PATH);
        if (!txtDir.exists()) txtDir.mkdirs();

        File sifreDosyasi = new File(TXT_PATH + "\\sifre.txt");
        boolean sifreBelirlenmis = false;
        String kayitliSifre = "";

        if (sifreDosyasi.exists()) {
            try {
                List<String> satirlar = Files.readAllLines(sifreDosyasi.toPath());
                if (!satirlar.isEmpty() && !satirlar.get(0).trim().isEmpty()) {
                    kayitliSifre = satirlar.get(0);
                    sifreBelirlenmis = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!sifreBelirlenmis) {
            String yeniSifre = JOptionPane.showInputDialog(this, "Yeni bir şifre belirleyin:");
            if (yeniSifre != null && !yeniSifre.trim().isEmpty()) {
                dosyayaYaz(sifreDosyasi, yeniSifre); // Dosyaya yazar (dosya boşsa içini doldurur)
                logYaz("Şifre oluşturuldu/Giriş yapıldı.");
            } else {
                System.exit(0); // İptale basılırsa çık
            }
        } else {
            sifreDogrula(kayitliSifre);
        }
    }

    private void sifreDogrula(String kayitliSifre) {
        while (sifreHataSayisi < 3) {
            String girilenSifre = JOptionPane.showInputDialog(this, "Lütfen şifrenizi girin:");

            if (girilenSifre == null) {
                System.exit(0);
            }

            logYaz("Şifre denemesi yapıldı.");

            if (girilenSifre.equals(kayitliSifre)) {
                logYaz("Başarılı giriş yapıldı.");
                return; // Doğruysa oyun ekranı açılacak
            } else {
                sifreHataSayisi++;
                JOptionPane.showMessageDialog(this, "Hatalı şifre! Kalan hak: " + (3 - sifreHataSayisi));
            }
        }
        JOptionPane.showMessageDialog(this, "3 hatalı giriş! Program kapatılıyor.");
        System.exit(0);
    }

    private void logYaz(String islem) {
        String tarihSaat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        String logMetni = tarihSaat + " - " + islem;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TXT_PATH + "\\log.txt", true))) {
            bw.write(logMetni);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void oyunuBaslat() {
        oyunDevamEdiyor = true;
        hataliGirisSayisi = 0;
        oyunSuresi = 0;
        resimGuncelle(1); // 1. resim
        sureLabel.setText("Süre: 0 sn");
        harfTextField.setText("");
        kelimeTextField.setText("");
        
        try {
            List<String> kelimeler = Files.readAllLines(Paths.get(TXT_PATH + "\\kelimeler.txt"));
            secilenKelime = kelimeler.get(new Random().nextInt(kelimeler.size())).toUpperCase();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "kelimeler.txt okunamadı!");
            return;
        }

        kelimePanel.removeAll();
        for (int i = 0; i < secilenKelime.length(); i++) {
            JLabel harfLabel = new JLabel("*");
            harfLabel.setFont(new java.awt.Font("Tahoma", 1, 24));
            kelimePanel.add(harfLabel);
        }
        kelimePanel.revalidate();
        kelimePanel.repaint();

        if (saniyeTimer != null) saniyeTimer.stop();
        saniyeTimer = new Timer(1000, e -> {
            oyunSuresi++;
            sureLabel.setText("Süre: " + oyunSuresi + " sn"); // Arayüzündeki labeli güncelle
        });
        saniyeTimer.start();
    }

    public void harfTahminEt(String harf) {
        if (!oyunDevamEdiyor) {
            JOptionPane.showMessageDialog(this, "Lütfen menüden oyunu başlatın!");
            return;
        }
        
        if (harf == null || harf.length() != 1) return;
        harf = harf.toUpperCase();
        boolean dogruMu = false;

        for (int i = 0; i < secilenKelime.length(); i++) {
            if (secilenKelime.charAt(i) == harf.charAt(0)) {
                JLabel label = (JLabel) kelimePanel.getComponent(i);
                label.setText(harf); // Harfler görüntülenmeli 
                dogruMu = true;
            }
        }
        tahminSonrasiKontrol(dogruMu);
    }

    public void kelimeTahminEt(String tahminEdilenKelime) {
        if (!oyunDevamEdiyor) {
            JOptionPane.showMessageDialog(this, "Lütfen menüden oyunu başlatın!");
            return;
        }
        
        if (tahminEdilenKelime.toUpperCase().equals(secilenKelime)) {
            for (int i = 0; i < secilenKelime.length(); i++) {
                JLabel label = (JLabel) kelimePanel.getComponent(i);
                label.setText(String.valueOf(secilenKelime.charAt(i)));
            }
            oyunuBitir(true);
        } else {
            tahminSonrasiKontrol(false);
        }
    }

    private void tahminSonrasiKontrol(boolean dogruMu) {
        if (!dogruMu) {
            hataliGirisSayisi++;
            resimGuncelle(hataliGirisSayisi + 1); // Adım adım resim açılıyor 

            // 11 defa yanlış tahminde oyun başarısız
            if (hataliGirisSayisi >= 11) {
                oyunuBitir(false);
            }
        } else {
            // Kelimenin tamamı bulundu mu kontrolü
            boolean bittiMi = true;
            for (java.awt.Component comp : kelimePanel.getComponents()) {
                if (((JLabel)comp).getText().equals("*")) {
                    bittiMi = false; break;
                }
            }
            if (bittiMi) oyunuBitir(true);
        }
    }

    private void resimGuncelle(int adim) {
        // 1.jpg, 2.jpg şeklinde güncellenecek
        if (adim > 11) adim = 11;
        ImageIcon icon = new ImageIcon(IMG_PATH + "\\" + adim + ".jpg");
        resimLabel.setIcon(icon); // Arayüzündeki label'a resmi set et
    }
    
    private void oyunuBitir(boolean kazandiMi) {
        oyunDevamEdiyor = false;
        saniyeTimer.stop();
        String sonuc = kazandiMi ? "Kazandı" : "Kaybetti";
        String tarih = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
 
        String kayit = tarih + "," + oyunSuresi + "," + sonuc;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TXT_PATH + "\\oyunlar.txt", true))) {
            bw.write(kayit);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, "Oyun Bitti! Sonuç: " + sonuc + "\nSüre: " + oyunSuresi + " sn\nKelime: " + secilenKelime);
        tablolariGuncelle(); // Eski skorları ve logları anında tabloya yansıtmak için
    }
    
    public void tablolariGuncelle() {
        // Skor tablosunu güncelle
        guncelleTabloModeli(skorTable, TXT_PATH + "\\oyunlar.txt", new String[]{"Tarih", "Süre (sn)", "Sonuç"});
        // Log tablosunu güncelle
        guncelleTabloModeli(logTable, TXT_PATH + "\\log.txt", new String[]{"Log Kayıtları"});
    }

    private void guncelleTabloModeli(JTable tablo, String dosyaYolu, String[] kolonlar) {
        DefaultTableModel model = new DefaultTableModel(kolonlar, 0);
        try {
            if (new File(dosyaYolu).exists()) {
                List<String> satirlar = Files.readAllLines(Paths.get(dosyaYolu));
                for (String satir : satirlar) {
                    model.addRow(satir.split(",")); // Virgüle göre ayırıp tabloya ekliyoruz
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        tablo.setModel(model);
    }

    // Temizle Butonları ActionPerformed için ortak metot:
    public void dosyaTemizle(String dosyaAdi) {
        try {
            String kayitliSifre = Files.readAllLines(Paths.get(TXT_PATH + "\\sifre.txt")).get(0);
            String girilenSifre = JOptionPane.showInputDialog(this, "Dosyayı temizlemek için şifreyi girin:");

            if (girilenSifre != null && girilenSifre.equals(kayitliSifre)) {
                new FileWriter(TXT_PATH + "\\" + dosyaAdi, false).close(); // Dosyanın içini boşaltır
                JOptionPane.showMessageDialog(this, "Dosya temizlendi.");
                tablolariGuncelle();
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı şifre!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Yardımcı dosya yazma metodu
    private void dosyayaYaz(File dosya, String metin) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dosya))) {
            bw.write(metin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        resimLabel = new javax.swing.JLabel();
        kelimePanel = new javax.swing.JPanel();
        harfTextField = new javax.swing.JTextField();
        kelimeTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        sureLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        skorTable = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        logTable = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout kelimePanelLayout = new javax.swing.GroupLayout(kelimePanel);
        kelimePanel.setLayout(kelimePanelLayout);
        kelimePanelLayout.setHorizontalGroup(
            kelimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        kelimePanelLayout.setVerticalGroup(
            kelimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jButton1.setText("Harf Tahmin Et");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setText("Kelime Tahmin Et");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        sureLabel.setText("Süre: 0 sn");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(harfTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(kelimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(resimLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 89, Short.MAX_VALUE)
                                .addComponent(kelimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)))
                .addGap(20, 20, 20))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(sureLabel))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(resimLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(kelimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(harfTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(kelimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Oyun Oynama", jPanel1);

        skorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(skorTable);

        jButton3.setText("Temizle");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(58, 58, 58))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addGap(0, 26, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Geçmiş Skorlar", jPanel2);

        logTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(logTable);

        jButton4.setText("Temizle");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(345, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(62, 62, 62))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addGap(0, 24, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Loglar", jPanel3);

        jMenu1.setText("Oyun");

        jMenuItem1.setText("Oyuna Başla");
        jMenuItem1.addActionListener(this::jMenuItem1ActionPerformed);
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Oyunu Yeniden Başlat");
        jMenuItem2.addActionListener(this::jMenuItem2ActionPerformed);
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Oyunu Sonlandır");
        jMenuItem3.addActionListener(this::jMenuItem3ActionPerformed);
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        if (!oyunDevamEdiyor) {
            logYaz("Oyuna Başla seçeneği ile yeni oyun başlatıldı.");
            oyunuBaslat();
        } else {
            JOptionPane.showMessageDialog(this, "Oyun şu an zaten devam ediyor! Kapatıp yenisini açmak için 'Yeniden Başlat'ı kullanın.");
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        harfTahminEt(harfTextField.getText());
        harfTextField.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        dosyaTemizle("oyunlar.txt");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        dosyaTemizle("log.txt");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        String tahmin = kelimeTextField.getText();

        // Boş giriş yapılmasını engelliyoruz
        if (tahmin != null && !tahmin.trim().isEmpty()) {
            kelimeTahminEt(tahmin); // Önceden yazdığımız tahmin metodunu çağırır
            kelimeTextField.setText(""); // Tahmin yapıldıktan sonra kutunun içini temizler (kullanım kolaylığı)
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        logYaz("Oyunu Yeniden Başlat seçeneği kullanıldı.");
        if(oyunDevamEdiyor) oyunuBaslat();
        else {
            JOptionPane.showMessageDialog(this, "İlk başlatmada 'Oyuna Başla' butonuna tıklayınız.");
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        if (oyunDevamEdiyor) {
            int secim = JOptionPane.showConfirmDialog(this, 
                    "Mevcut oyunu sonlandırmak istediğinize emin misiniz?", 
                    "Oyunu Sonlandır", 
                    JOptionPane.YES_NO_OPTION);

            if (secim == JOptionPane.YES_OPTION) {
                logYaz("Oyunu Sonlandır seçeneği ile oyun yarıda kesildi.");

                for (int i = 0; i < secilenKelime.length(); i++) {
                    JLabel label = (JLabel) kelimePanel.getComponent(i);
                    label.setText(String.valueOf(secilenKelime.charAt(i)));
                }
                kelimePanel.paintImmediately(kelimePanel.getVisibleRect());

                oyunuBitir(false); 

                kelimePanel.removeAll();
                kelimePanel.revalidate();
                kelimePanel.repaint();
                sureLabel.setText("Süre: 0 sn");
                resimGuncelle(1);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Şu anda devam eden bir oyun bulunmuyor!");
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new P2Oyun().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField harfTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel kelimePanel;
    private javax.swing.JTextField kelimeTextField;
    private javax.swing.JTable logTable;
    private javax.swing.JLabel resimLabel;
    private javax.swing.JTable skorTable;
    private javax.swing.JLabel sureLabel;
    // End of variables declaration//GEN-END:variables
}
