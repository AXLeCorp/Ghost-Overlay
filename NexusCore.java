import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

public class NexusCore extends JWindow implements NativeKeyListener {
    private int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
    private int sh = Toolkit.getDefaultToolkit().getScreenSize().height;
    private int width = (int)(sw * 0.92), height = (int)(sh * 0.82); 
    private double currentY = -height - 150; 
    private boolean isVisible = false;
    
    // ЭФФЕКТЫ (Custom Engine)
    private float glassOpacity = 140f;
    private int glassHue = 10; 
    private double animSpeed = 0.12; 
    private ArrayList<Particle> particles = new ArrayList<>();
    private Timer fxTimer;

    private boolean settingsOpen = false;
    private int settingsX = -450; 
    private Timer anim, settingsAnim;
    private JPanel mainPanel;
    private JButton gearBtn, closeSettingsBtn;
    private JLabel lTitle;
    
    // Переменные настроек
    private JSlider sOpacity, sHue, sSpeed;
    private JLabel lblOp, lblHue, lblSp;

    public NexusCore() {
        setBackground(new Color(0,0,0,0));
        setSize(width, height);
        setLocation((sw - width) / 2, (int)currentY);
        setAlwaysOnTop(true);

        // Инициализация частиц для эффекта "живого" оверлея
        for(int i=0; i<30; i++) particles.add(new Particle(width, height));

        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 1. СТЕКЛЯННЫЙ КОРПУС С ГРАДИЕНТОМ
                LinearGradientPaint glassGrad = new LinearGradientPaint(0, 0, 0, getHeight(),
                    new float[]{0f, 0.5f, 1f}, 
                    new Color[]{new Color(glassHue, glassHue, glassHue + 20, (int)glassOpacity), 
                                new Color(glassHue+5, glassHue+5, glassHue+10, (int)glassOpacity-20),
                                new Color(glassHue, glassHue, glassHue, (int)glassOpacity)});
                g2.setPaint(glassGrad);
                g2.fill(new RoundRectangle2D.Double(10, 10, getWidth()-20, getHeight()-20, 70, 70));

                // 2. ФОНОВАЯ СЕТКА (Cyber Grid)
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(255, 255, 255, 15));
                for(int i = 40; i < getWidth(); i += 60) g2.drawLine(i, 20, i, getHeight()-20);
                for(int i = 40; i < getHeight(); i += 60) g2.drawLine(20, i, getWidth()-20, i);

                // 3. СИСТЕМА ЧАСТИЦ (Движутся в реальном времени)
                g2.setColor(new Color(0, 255, 255, 60));
                for(Particle p : particles) {
                    p.update();
                    g2.fillOval((int)p.x, (int)p.y, p.size, p.size);
                }

                // 4. КОНТУР И БЛИК
                g2.setStroke(new BasicStroke(2.5f));
                g2.setPaint(new Color(0, 255, 200, 80));
                g2.draw(new RoundRectangle2D.Double(10, 10, getWidth()-20, getHeight()-20, 70, 70));

                // ПАНЕЛЬ НАСТРОЕК
                if (settingsX > -450) {
                    g2.setPaint(new Color(0, 0, 0, 245));
                    g2.fill(new RoundRectangle2D.Double(settingsX, 40, 350, getHeight()-80, 50, 50));
                    g2.setColor(new Color(0, 255, 255, 150));
                    g2.draw(new RoundRectangle2D.Double(settingsX, 40, 350, getHeight()-80, 50, 50));
                }
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setOpaque(false);

        initUI();
        add(mainPanel);

        // Таймер для отрисовки частиц (60 FPS)
        fxTimer = new Timer(16, e -> mainPanel.repaint());
        fxTimer.start();

        setupAnimations();
        setVisible(true);
    }

    private void setupAnimations() {
        settingsAnim = new Timer(7, e -> {
            int targetX = settingsOpen ? 45 : -450;
            settingsX += (targetX - settingsX) * 0.2;
            syncUI();
            if (Math.abs(settingsX - targetX) < 1) { settingsX = targetX; settingsAnim.stop(); }
        });

        anim = new Timer(5, e -> {
            double targetY = isVisible ? 25 : -getHeight() - 150;
            currentY += (targetY - currentY) * animSpeed;
            setLocation((sw - width) / 2, (int)currentY);
            if (Math.abs(targetY - currentY) < 1) {
                currentY = targetY;
                if (!isVisible) setUIActive(false);
                anim.stop();
            }
        });
    }

    private void initUI() {
        gearBtn = createBtn("✦", 60, 60, 60, 60, 45); // Иконка звезды вместо шестеренки
        gearBtn.setForeground(new Color(0, 255, 255));
        gearBtn.addActionListener(e -> { settingsOpen = true; gearBtn.setVisible(false); setUIActive(true); settingsAnim.start(); });

        closeSettingsBtn = createBtn("✕", -450, 60, 50, 50, 24);
        closeSettingsBtn.addActionListener(e -> { settingsOpen = false; settingsAnim.start(); });

        lTitle = createLabel("GHOST CORE ENGINE", 75, 24);
        lTitle.setForeground(new Color(0, 255, 255));

        lblOp = createLabel("Ghost Density", 140, 14);
        sOpacity = createSlider(165, 50, 255, (int)glassOpacity);
        sOpacity.addChangeListener(e -> glassOpacity = sOpacity.getValue());

        lblHue = createLabel("Core Frequency", 220, 14);
        sHue = createSlider(245, 0, 150, glassHue);
        sHue.addChangeListener(e -> glassHue = sHue.getValue());

        lblSp = createLabel("Warp Speed", 300, 14);
        sSpeed = createSlider(325, 5, 40, (int)(animSpeed * 100));
        sSpeed.addChangeListener(e -> animSpeed = sSpeed.getValue() / 100.0);

        mainPanel.add(gearBtn); mainPanel.add(closeSettingsBtn); 
        mainPanel.add(lTitle); mainPanel.add(lblOp); mainPanel.add(sOpacity);
        mainPanel.add(lblHue); mainPanel.add(sHue); mainPanel.add(lblSp); mainPanel.add(sSpeed);
    }

    private void syncUI() {
        int left = settingsX + 50;
        lTitle.setLocation(left, 75);
        closeSettingsBtn.setLocation(settingsX + 270, 65);
        lblOp.setLocation(left, 140); sOpacity.setLocation(left, 165);
        lblHue.setLocation(left, 220); sHue.setLocation(left, 245);
        lblSp.setLocation(left, 300); sSpeed.setLocation(left, 325);
        if (!settingsOpen && settingsX < -440) gearBtn.setVisible(isVisible);
    }

    private void setUIActive(boolean v) {
        lTitle.setVisible(v); sOpacity.setVisible(v); sHue.setVisible(v); sSpeed.setVisible(v);
        lblOp.setVisible(v); lblHue.setVisible(v); lblSp.setVisible(v); closeSettingsBtn.setVisible(v);
    }

    private JSlider createSlider(int y, int min, int max, int val) {
        JSlider s = new JSlider(min, max, val);
        s.setBounds(-450, y, 250, 30);
        s.setOpaque(false);
        return s;
    }

    private JLabel createLabel(String txt, int y, int size) {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Consolas", Font.BOLD, size));
        l.setBounds(-450, y, 300, 30);
        return l;
    }

    private JButton createBtn(String txt, int x, int y, int w, int h, int size) {
        JButton b = new JButton(txt);
        b.setBounds(x, y, w, h);
        b.setFont(new Font("Segoe UI Semibold", Font.PLAIN, size));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1 && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            isVisible = !isVisible;
            if (isVisible) { gearBtn.setVisible(true); this.toFront(); }
            anim.start();
        }
    }
    public void nativeKeyReleased(NativeKeyEvent e) {}
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void main(String[] args) {
        try { GlobalScreen.registerNativeHook(); } catch (Exception ex) { System.exit(1); }
        GlobalScreen.addNativeKeyListener(new NexusCore());
    }

    // Класс для частиц (тот самый эффект "жизни")
    class Particle {
        double x, y, dx, dy;
        int size;
        int mw, mh;
        Particle(int w, int h) {
            mw = w; mh = h;
            Random r = new Random();
            x = r.nextInt(w); y = r.nextInt(h);
            dx = (r.nextDouble() - 0.5) * 2;
            dy = (r.nextDouble() - 0.5) * 2;
            size = r.nextInt(4) + 2;
        }
        void update() {
            x += dx; y += dy;
            if (x < 0 || x > mw) dx *= -1;
            if (y < 0 || y > mh) dy *= -1;
        }
    }
}