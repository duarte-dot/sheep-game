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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable {

    // Variáveis de Dimensão da Janela
    private final int WIDTH = 160;
    private final int HEIGHT = 120;
    private final int SCALE = 3;

    // Componentes Gráficos
    private BufferedImage image;
    private Spritesheet sheet;
    private BufferedImage[] player;

    // Controle de Jogo
    private Thread thread;
    private boolean isRunning = false;
    private int frames = 0;
    private int maxFrames = 20;
    private int currAnimation = 0, maxAnimation = 2;

    // Posição e Movimento do Jogador
    private int playerX = 20;
    private int playerY = 20;
    private int playerSpeed = 1;
    private int playerDirectionX = 1;
    private int playerDirectionY = 1;

    // Controle de Teclas Pressionadas
    private boolean rightPressed = false;
    private boolean leftPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    // Janela do Jogo
    public static JFrame frame;

    // Construtor
    public Game() {
        // Inicialização dos Componentes Gráficos
        sheet = new Spritesheet("/spritesheet.png");
        player = new BufferedImage[2];
        player[0] = sheet.getSprite(0, 0, 16, 16);
        player[1] = sheet.getSprite(16, 0, 16, 16);
        this.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        setFocusable(false);
        initFrame();
    }

    // Método para Inicialização da Janela do Jogo
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

    // Métodos de Controle de Thread do Jogo
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

    // Método Principal do Jogo
    public static void main(String args[]) {
        Game game = new Game();
        game.start();
    }

    // Métodos de Atualização do Jogo
    public void tick() {
        frames++;
        if (frames > maxFrames) {
            frames = 0;
            currAnimation++;
            if (currAnimation >= maxAnimation) {
                currAnimation = 0;
            }
        }

        updatePlayerPosition();
    }

    private void updatePlayerPosition() {
        if (leftPressed) {
            playerX -= playerSpeed;
            if (playerX < 0) {
                playerX = WIDTH - player[1].getWidth() + 16;
            }
        }
        if (rightPressed) {
            playerX += playerSpeed;
            if (playerX > WIDTH - player[0].getWidth() + 16) {
                playerX = 0;
            }
        }
        if (upPressed) {
            playerY -= playerSpeed;
            if (playerY < 0 - 8) {
                playerY = HEIGHT - player[0].getHeight() + 8;
            }
        }
        if (downPressed) {
            playerY += playerSpeed;
            if (playerY > HEIGHT - player[0].getHeight() + 8) {
                playerY = 0 - 8;
            }
        }
        if (!leftPressed && !rightPressed && !upPressed && !downPressed) {
            currAnimation = 0;
        }
    }

    // Métodos de Renderização do Jogo
    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = image.getGraphics();
        g.setColor(new Color(19, 19, 19));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Graphics2D g2 = (Graphics2D) g;

        renderPlayer(g2);

        g.dispose();
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
        bs.show();
    }
    
    public void drawPlayer(Graphics2D g2, int x, int y) {
        g2.drawImage(player[currAnimation], x, y, null);
    }
    
    public void drawReversedPlayer(Graphics2D g2, int x, int y) {
    	AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-player[currAnimation].getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage reversedImage = op.filter(player[currAnimation], null);
        g2.drawImage(reversedImage, x, y, null);
    }

    private void renderPlayer(Graphics2D g2) {
        int oppositeX = (playerX < 0) ? playerX - WIDTH : playerX - WIDTH;
        int oppositeY = (playerY < 0) ? playerY + HEIGHT : playerY - HEIGHT;

        if (playerX < 0 || playerX + 16 > WIDTH) {
            if (playerDirectionX == 1) {
            	drawPlayer(g2, playerX, playerY);
            } else {
                drawReversedPlayer(g2, oppositeX, playerY);
            }

            if (playerDirectionX == -1) {
            	drawReversedPlayer(g2, playerX, playerY);
            } else {
            	drawPlayer(g2, oppositeX, playerY);
            }
        } else {
            if (playerDirectionX == -1) {
            	drawReversedPlayer(g2, playerX, playerY);
            } else {
            	drawPlayer(g2, playerX, playerY);
            }
        }

        if (playerY < 0 || playerY + player[currAnimation].getHeight() > HEIGHT) {
            if (playerDirectionX == -1) {
            	drawReversedPlayer(g2, playerX, oppositeY);
            } else {
            	drawPlayer(g2, playerX, oppositeY);
            }
        }
    }

    // Métodos de Controle de Teclado
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            upPressed = true;
            playerDirectionY = -1;
        } else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            downPressed = true;
            playerDirectionY = 1;
        } else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            leftPressed = true;
            playerDirectionX = -1;
        } else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
            playerDirectionX = 1;
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

    // Método de execução da Thread
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        int frames = 0;
        double timer = System.currentTimeMillis();
        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;

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
