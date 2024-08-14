
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class juegopong extends JPanel implements ActionListener {

   
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int COURT_WIDTH = 700;
    private static final int COURT_HEIGHT = 500;
    private static final int PADDLE_WIDTH = 10;
    private static final int PADDLE_HEIGHT = 100;
    private static final int BALL_SIZE = 20;
    private static final int POINTS_TO_WIN = 7;
    private static final int MAX_SETS = 3;


    private int player1Y = COURT_HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int player2Y = COURT_HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int ballX = COURT_WIDTH / 2 - BALL_SIZE / 2;
    private int ballY = COURT_HEIGHT / 2 - BALL_SIZE / 2;
    private int ballXSpeed = 5;
    private int ballYSpeed = 5;
    private int player1Score = 0;
    private int player2Score = 0;
    private int currentSet = 1;
    private boolean isPaused = false;
    private boolean gameOver = false;

    private Timer timer;
    private boolean player1Up = false;
    private boolean player1Down = false;
    private boolean player2Up = false;
    private boolean player2Down = false;

    public juegopong() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        requestFocus();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
               
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    player1Up = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    player1Down = true;
                }

             
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    player2Up = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    player2Down = true;
                }

            
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause();
                }

              
                if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
                    resetGame();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
              
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    player1Up = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    player1Down = false;
                }

              
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    player2Up = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    player2Down = false;
                }
            }
        });

        timer = new Timer(10, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

  
        g.setColor(Color.BLACK);
        g.drawRect((WIDTH - COURT_WIDTH) / 2, (HEIGHT - COURT_HEIGHT) / 2, COURT_WIDTH, COURT_HEIGHT);

      
        g.setColor(Color.BLACK);
        g.fillRect((WIDTH - COURT_WIDTH) / 2 - PADDLE_WIDTH, player1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect((WIDTH + COURT_WIDTH) / 2, player2Y, PADDLE_WIDTH, PADDLE_HEIGHT);

        
        g.setColor(Color.BLACK);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

       
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Jugador 1: " + player1Score, 20, 30);
        g.drawString("Jugador 2: " + player2Score, WIDTH - 150, 30);

       
        if (isPaused) {
            drawPauseMenu(g);
        }

       
        if (gameOver) {
            drawGameOverScreen(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused && !gameOver) {
            movePaddles();
            moveBall();
            checkCollisions();
            checkWin();
            repaint();
        }
    }

    private void movePaddles() {
       
        if (player1Up && player1Y > (HEIGHT - COURT_HEIGHT) / 2) {
            player1Y -= 10;
        }
        if (player1Down && player1Y < (HEIGHT - COURT_HEIGHT) / 2 + COURT_HEIGHT - PADDLE_HEIGHT) {
            player1Y += 10;
        }

       
        if (player2Up && player2Y > (HEIGHT - COURT_HEIGHT) / 2) {
            player2Y -= 10;
        }
        if (player2Down && player2Y < (HEIGHT - COURT_HEIGHT) / 2 + COURT_HEIGHT - PADDLE_HEIGHT) {
            player2Y += 10;
        }
    }

    private void moveBall() {
        ballX += ballXSpeed;
        ballY += ballYSpeed;
    }

    private void checkCollisions() {
       
        int x = (WIDTH - COURT_WIDTH) / 2;
        int y = (HEIGHT - COURT_HEIGHT) / 2;

        if (ballY <= y || ballY >= y + COURT_HEIGHT - BALL_SIZE) {
            ballYSpeed = -ballYSpeed;
        }

       
        if (ballX <= x + PADDLE_WIDTH && ballY + BALL_SIZE >= player1Y && ballY <= player1Y + PADDLE_HEIGHT) {
            ballXSpeed = -ballXSpeed;
        }
        if (ballX >= x + COURT_WIDTH - PADDLE_WIDTH - BALL_SIZE && ballY + BALL_SIZE >= player2Y && ballY <= player2Y + PADDLE_HEIGHT) {
            ballXSpeed = -ballXSpeed;
        }

        
        if (ballX < x) {
            player2Score++;
            resetBall(true); 
        }
        if (ballX > x + COURT_WIDTH) {
            player1Score++;
            resetBall(false); 
        }
    }

    private void checkWin() {
        if (player1Score >= POINTS_TO_WIN || player2Score >= POINTS_TO_WIN) {
            if (player1Score == POINTS_TO_WIN) {
                currentSet++;
            } else {
                currentSet++;
            }
            if (currentSet > MAX_SETS) {
                gameOver = true;
            } else {
                resetScores();
                resetBall(true); 
            }
        }
    }

    private void resetBall(boolean player1Starts) {
        ballX = COURT_WIDTH / 2 - BALL_SIZE / 2;
        ballY = COURT_HEIGHT / 2 - BALL_SIZE / 2;
        ballXSpeed = player1Starts ? 5 : -5; 
        ballYSpeed = 5;
    }

    private void resetScores() {
        player1Score = 0;
        player2Score = 0;
    }

    private void togglePause() {
        isPaused = !isPaused;
        repaint();
    }

    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Juego pausado", WIDTH / 2 - 150, HEIGHT / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Presiona Esc o P para reanudar", WIDTH / 2 - 150, HEIGHT / 2);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        if (player1Score > player2Score) {
            g.drawString("¡Jugador 1 gana!", WIDTH / 2 - 150, HEIGHT / 2 - 50);
        } else {
            g.drawString("¡Jugador 2 gana!", WIDTH / 2 - 150, HEIGHT / 2 - 50);
        }
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Presiona Enter para jugar de nuevo", WIDTH / 2 - 150, HEIGHT / 2);
    }

    private void resetGame() {
        player1Score = 0;
        player2Score = 0;
        currentSet = 1;
        gameOver = false;
        resetBall(true); 
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong Game");
        juegopong pongGame = new juegopong();
        frame.add(pongGame);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

