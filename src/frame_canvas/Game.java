package frame_canvas;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable {

    public static JFrame frame;
    private Thread thread;
    private boolean isRunning = false;
    private final int WIDTH = 160;
    private final int HEIGHT = 120;
    private final int SCALE = 3;

    private BufferedImage image;

    private Spritesheet sheet;
    private BufferedImage[] player;
    private int frames = 0;
    private int maxFrames = 20;
    private int currAnimation = 0, maxAnimation = 2;
    
    private int playerX = 20; // posição inicial do jogador em X
    private int playerY = 20; // posição inicial do jogador em Y
    private int playerSpeed = 1; // velocidade de movimento do jogador
    private int playerDirectionX = 1; // 1 para direita, -1 para esquerda
    private int playerDirectionY = 1; // 1 para baixo, -1 para cima
    
    private boolean rightPressed = false;
    private boolean leftPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    public Game() {
        sheet = new Spritesheet("/spritesheet.png");
        player = new BufferedImage[2];
        player[0] = sheet.getSprite(0,  0, 16, 16);
        player[1] = sheet.getSprite(16, 0, 16, 16);
        this.setPreferredSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));
        initFrame();
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    public void initFrame() {
        frame = new JFrame();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Game.this.keyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
                Game.this.keyReleased(e);
            }
        });

        // Adicionando um MouseListener vazio
        frame.addMouseListener(new MouseAdapter() {
            // Nada será feito nos métodos do MouseListener
        });

        frame.add(this);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        frame.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("A janela está focada.");
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("A janela perdeu o foco.");
            }
        });
    }

    public synchronized void start() {
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    public synchronized void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Game game = new Game();
        game.start();
    }

    public void tick() {
        frames++;
        if (frames > maxFrames) {
            frames = 0;
            currAnimation++;

            if (currAnimation >= maxAnimation) {
                currAnimation = 0;
            }
        }
        
        // Atualiza a posição do jogador de acordo com as teclas pressionadas
        // e verifica se atingiu os limites da janela
        if (leftPressed) {
            playerX -= playerSpeed;
            if (playerX < -16) {
                playerX = WIDTH - player[0].getWidth() + 16;
            }
        }
        if (rightPressed) {
            playerX += playerSpeed;
            if (playerX > WIDTH - player[0].getWidth() + 16) {
                playerX = -16;
            }
        }
        if (upPressed) {
            playerY -= playerSpeed;
            if (playerY < 0 - 16) {
                playerY = HEIGHT - player[0].getHeight() + 16;
            }
        }
        if (downPressed) {
            playerY += playerSpeed;
            if (playerY > HEIGHT - player[0].getHeight() + 16) {
                playerY = 0 - 16;
            }
        }
        
        // Verifica se o jogador está parado e define a animação de parado
        if (!leftPressed && !rightPressed && !upPressed && !downPressed) {
            currAnimation = 0; // Define a animação para a sprite de parado
        }
    }


    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            // Movimento para cima
            upPressed = true;
            playerDirectionY = -1; // Define a direção vertical para cima
        } else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            // Movimento para baixo
            downPressed = true;
            playerDirectionY = 1; // Define a direção vertical para baixo
        } else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            // Movimento para a esquerda
            leftPressed = true;
            playerDirectionX = -1; // Define a direção horizontal para esquerda
        } else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            // Movimento para a direita
            rightPressed = true;
            playerDirectionX = 1; // Define a direção horizontal para direita
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            upPressed = false;
        } else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            downPressed = false;
        } else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        } else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
    


    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = image.getGraphics();
        g.setColor(new Color(19, 19, 19));
        g.fillRect(0,  0, WIDTH, HEIGHT);

        Graphics2D g2 = (Graphics2D) g;

        // Verifica se o jogador está se movendo para a esquerda
        if (playerDirectionX == -1) { // Esquerda
            // Cria uma transformação para inverter horizontalmente
            AffineTransform transform = new AffineTransform();
            transform.translate(player[currAnimation].getWidth(), 0);
            transform.scale(-1, 1);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            // Aplica a transformação no sprite e desenha o sprite invertido horizontalmente
            BufferedImage invertedPlayer = op.filter(player[currAnimation], null);
            g2.drawImage(invertedPlayer, playerX, playerY, null);
        } else { // Direita
            g2.drawImage(player[currAnimation], playerX, playerY, null);
        }

        g.dispose();
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH*SCALE, HEIGHT*SCALE, null);
        bs.show();
    }

    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        int frames = 0;
        double timer = System.currentTimeMillis();
        while(isRunning) {
            long now = System.nanoTime();
            delta+= (now - lastTime) / ns;

            lastTime = now;

            if (delta >= 1) {
                tick();
                render();
                frames++;
                delta--;
            }

            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS:" + frames);
                frames = 0;
                timer += 1000;
            }
        }

        stop();
    }
}
