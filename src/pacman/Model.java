package pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Model extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    protected boolean dying = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;

    private int N_GHOSTS = 6;
    private int lives, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy;

    private final int[] validSpeeds = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;
    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    private final short[] levelData = {
            // 0=blue, 1=left border, 2=top border,
            // 4=right border, 8=bottom border, 16=white dots
            // liczby odpowiadają sumie powyższych
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0, 0, 0, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 0, 0, 0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 0, 0, 0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0, 0, 0, 0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 0, 0, 0, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    public Model() {
        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }

    private void loadImages() {
        down = new ImageIcon("scr/pacman/images/down.gif").getImage();
        up = new ImageIcon("scr/pacman/images/up.gif").getImage();
        left = new ImageIcon("scr/pacman/images/left.gif").getImage();
        right = new ImageIcon("scr/pacman/images/right.gif").getImage();
        ghost = new ImageIcon("src/pacman/images/ghost.gif").getImage();
//        heart = new ImageIcon("scr/pacman/images/heart.gif").getImage();
        heart = new ImageIcon("scr/pacman/images/ghost.gif").getImage();
    }


    public void showIntroScreen(Graphics2D g2D) {
        String start = "Naciśnij SPACE aby zacząć.";
        g2D.setColor(Color.yellow);
        g2D.drawString(start, SCREEN_SIZE / 4, 150);
    }

    public void drawScore(Graphics2D g2D) {
        g2D.setFont(smallFont);
        g2D.setColor(new Color(5, 151, 79));
        String s = "Wynik: " + score;
        g2D.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (int i = 0; i < lives; i++) {
//            g2D.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
            g2D.drawImage(ghost, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void initVariables() {
        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.restart();
    }

    private void initGame() {
        lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel() {
        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }
    }

    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE; //początkowa pozycja
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7 * BLOCK_SIZE;  //początkowa pozycja
        pacman_y = 11 * BLOCK_SIZE;
        pacmand_x = 0;    //reset kierunku
        pacmand_y = 0;
        req_dx = 0;        // reset kontroli kierunku
        req_dy = 0;
        dying = false;
    }

    private void playGame(Graphics2D g2D) {
        if (dying) {
            death();
        } else {
            movePacman();
            drawPacman(g2D);
            moveGhosts(g2D);
            checkMaze();
        }
    }

    private void movePacman() {

        int pos;
        short ch;

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                }
            }

            // sprawdza czy nadal jest
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0;
                pacmand_y = 0;
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void drawPacman(Graphics2D g2D) {

//        g2D.drawImage(ghost, pacman_x, pacman_y, this); // testowy wizerunek pacman

        if (req_dx == -1) {
//            g2D.drawImage(left, pacman_x + 1, pacman_y + 1, this);
            g2D.drawImage(ghost, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dx == 1) {
//            g2D.drawImage(right, pacman_x + 1, pacman_y + 1, this);
            g2D.drawImage(ghost, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dy == -1) {
//            g2D.drawImage(up, pacman_x + 1, pacman_y + 1, this);
            g2D.drawImage(ghost, pacman_x + 1, pacman_y + 1, this);
        } else {
//            g2D.drawImage(down, pacman_x + 1, pacman_y + 1, this);
            g2D.drawImage(ghost, pacman_x + 1, pacman_y + 1, this);
        }
    }

    private void moveGhosts(Graphics2D g2D) {
        int pos;
        int count;
        for (int i = 0; i < N_GHOSTS; i++) {
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (ghost_y[i] / BLOCK_SIZE);

                count = 0;
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }
            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2D, ghost_x[i] + 1, ghost_y[i] + 1);

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2D, int x, int y) {
        g2D.drawImage(ghost, x, y, this);
    }

    private void checkMaze() {
        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }
            i++;
        }

        if (finished) {
            score += 50;

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

        lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void drawMaze(Graphics2D g2D) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2D.setColor(new Color(0, 72, 251));
                g2D.setStroke(new BasicStroke(5));

                if (levelData[i] == 0) {
                    g2D.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }

                if ((screenData[i] & 1) != 0) {
                    g2D.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2D.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2D.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2D.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2D.setColor(new Color(255, 255, 255));
                    g2D.fillOval(x + 10, y + 10, 6, 6);
                }
                i++;
            }
        }

    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;

        g2D.setColor(Color.black);
        g2D.fillRect(0, 0, d.width, d.height);

        drawMaze(g2D);
        drawScore(g2D);

        if (inGame) {
            playGame(g2D);
        } else {
            showIntroScreen(g2D);
        }

        Toolkit.getDefaultToolkit().sync();
        g2D.dispose();
    }

    //kontrola
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
