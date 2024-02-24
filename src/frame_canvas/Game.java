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
        setFocusable(false);
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
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Graphics2D g2 = (Graphics2D) g;

        // Verifica se a parte da ovelha está fora da tela na horizontal
     // Calcula a posição X da parte oposta da ovelha
        int oppositeX = (playerX < 0) ? playerX - WIDTH : playerX - WIDTH;
        
        if (playerX < 0 || playerX + 16 > WIDTH) {
        	System.out.println("menor que 0 ou maior que width");
            if (playerDirectionX == 1) { // Direita
//            	System.out.println("direita 1");
                g2.drawImage(player[currAnimation], playerX, playerY, null);
            } else { // Esquerda
            	System.out.println("esquerda 1");
            	 // Inverte a imagem horizontalmente
                AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-player[currAnimation].getWidth(null), 0);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                BufferedImage reversedImage = op.filter(player[currAnimation], null);
                g2.drawImage(reversedImage, oppositeX, playerY, null);
            }

            
            // Desenha a parte oposta da ovelha
            if (playerDirectionX == -1) { // Esquerda
            	 AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
               tx.translate(-player[currAnimation].getWidth(null), 0);
               AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
               BufferedImage reversedImage = op.filter(player[currAnimation], null);
               g2.drawImage(reversedImage, playerX, playerY, null);
            } else { // Direita
                g2.drawImage(player[currAnimation], oppositeX, playerY, null);
            }
        } else {
            // Desenha a ovelha normalmente sem considerar a parte oposta
            if (playerDirectionX == -1) { // Esquerda
            	System.out.println("esquerda 2");
            	 // Inverte a imagem horizontalmente
                AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-player[currAnimation].getWidth(null), 0);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                BufferedImage reversedImage = op.filter(player[currAnimation], null);
                g2.drawImage(reversedImage, playerX, playerY, null);            
                } else { // Direita
//                	System.out.println("direita 2");
                g2.drawImage(player[currAnimation], playerX, playerY, null);
            }
        }

        // Verifica se a parte da ovelha está fora da tela na vertical
        if (playerY < 0 || playerY + player[currAnimation].getHeight() > HEIGHT) {
            // Calcula a posição Y da parte oposta da ovelha
            int oppositeY = (playerY < 0) ? playerY + HEIGHT : playerY - HEIGHT;
            if (playerDirectionX == -1) { // Esquerda
            	System.out.println("esquerda 2");
            	// Inverte a imagem horizontalmente
                AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-player[currAnimation].getWidth(null), 0);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                BufferedImage reversedImage = op.filter(player[currAnimation], null);
                g2.drawImage(reversedImage, playerX, oppositeY, null);            
                } else { // Direita
//                	System.out.println("direita 2");
                g2.drawImage(player[currAnimation], playerX, oppositeY, null);
            }
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
