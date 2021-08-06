package cc.eumc.screenmirroringclient.view;

import cc.eumc.screenmirroringclient.SlidesClient;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class SlideshowView {
    private SlidesClient slidesClient;

    private JLabel imagePreview = new JLabel(new ImageIcon());

    public SlideshowView(SlidesClient slidesClient) {
        this.slidesClient = slidesClient;

        // setup look and feel
        FlatLightLaf.setup();

        JFrame frame = new JFrame("Slides - EusScreenMirroring Client");
        frame.setLayout(new BorderLayout());
        frame.setSize(350,350);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(imagePreview);

        frame.setVisible(true);
    }
}
