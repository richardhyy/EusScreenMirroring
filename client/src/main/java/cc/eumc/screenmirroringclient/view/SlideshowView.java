package cc.eumc.screenmirroringclient.view;

import cc.eumc.screenmirroringclient.SlidesClient;
import cc.eumc.screenmirroringclient.model.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class SlideshowView {
    private SlidesClient slidesClient;

    private JFrame frame = new JFrame("Slides - EusScreenMirroring Client");
    private JLabel currentPagePreview = new JLabel();
    private JButton[] pagePreviews;
    private JScrollPane scrollPane = new JScrollPane();

    RefreshPageLaterThread refreshTask = null;

    public SlideshowView(SlidesClient slidesClient) {
        this.slidesClient = slidesClient;

//        // setup look and feel
//        FlatLightLaf.setup();

        frame.setLayout(null);
        frame.setSize(slidesClient.getScreen().getWidth() / 2,slidesClient.getScreen().getHeight() / 2);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(currentPagePreview);
        frame.add(scrollPane);

        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                onResize();
            }
        });

        currentPagePreview.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JLabel source = (JLabel) e.getSource();
                Screen screen = slidesClient.getScreen();
//                int[] coordinates = screen.toScaledScreenCoordinates((int) ((float) screen.getWidth() / (float) source.getWidth() * (float) e.getX()),
//                                                                     (int) ((float) screen.getHeight() / (float) source.getHeight() * (float) e.getY()));
                slidesClient.setMouseLocation((short) ((float) screen.getWidth() / (float) source.getWidth() * (float) e.getX()),
                                              (short) ((float) screen.getHeight() / (float) source.getHeight() * (float) e.getY()));
            }
        });

        currentPagePreview.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() > 0) {
                    slidesClient.nextPage();
                } else {
                    slidesClient.previousPage();
                }

                refreshPageLater();
            }
        });

        currentPagePreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    slidesClient.nextPage();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    slidesClient.previousPage();
                }
                loadPageImage();

                refreshPageLater();
            }
        });

        onResize();
        loadPageImage();
    }

    private void refreshPageLater() {
        if (refreshTask != null) {
            refreshTask.setCancelled();
        }

        refreshTask = new RefreshPageLaterThread();

        refreshTask.start();

        loadPageImage();
    }

    private void loadPageImage() {
        BufferedImage[] images = slidesClient.getPagePreviews();
        currentPagePreview.setIcon(getScaledIcon(images[slidesClient.getCurrentPage()], currentPagePreview.getWidth(), currentPagePreview.getHeight()));
//        scrollPane.removeAll();
//        pagePreviews = new JButton[slidesClient.getTotalPages()];
//        for (int i = 0; i < pagePreviews.length; i++) {
//            pagePreviews[i] = new JButton(getScaledIcon(images[i], 120, 120));
//            pagePreviews[i].setBounds(4, 5 + i * 128, 120, 120);
//            int currentPage = i;
//            pagePreviews[i].addActionListener(e -> slidesClient.sendPage(currentPage));
//            scrollPane.add(pagePreviews[i]);
//        }
//        scrollPane.set
    }

    private ImageIcon getScaledIcon(BufferedImage image, int width, int height) {
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_FAST);
        return new ImageIcon(scaled);
    }

    private void onResize() {
        currentPagePreview.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        loadPageImage();
//        scrollPane.setBounds(0, 0, 120, frame.getHeight());
    }

    class RefreshPageLaterThread extends Thread {
        private boolean cancelled = false;

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                if (cancelled) {
                    return;
                }
            } catch (InterruptedException ignore) {}
            slidesClient.sendPage(slidesClient.getCurrentPage());
        }

        public void setCancelled() {
            this.cancelled = true;
        }
    }
}
