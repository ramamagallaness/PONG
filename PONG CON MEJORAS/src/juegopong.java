import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class juegopong extends JPanel implements ActionListener {
    private static final int WIDTH = 1000, HEIGHT = 600, COURT_WIDTH = 900, COURT_HEIGHT = 500;
    private static final int PADDLE_WIDTH = 10, PADDLE_HEIGHT = 100, BALL_SIZE = 20;
    private static final int POINTS_TO_WIN = 7, GAME_TIME = 60 * 1000, MAX_BALL_SPEED = 10;
    private static final double SPEED_INCREMENT = 1.2;
    private static final int MINI_PAUSE_TIME = 5 * 1000;

    private long remainingTime = GAME_TIME, lastUpdateTime = System.currentTimeMillis(), miniPauseStartTime;
    private int player1Y = COURT_HEIGHT / 2 - PADDLE_HEIGHT / 2, player2Y = COURT_HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int ballX = COURT_WIDTH / 2 - BALL_SIZE / 2, ballY = COURT_HEIGHT / 2 - BALL_SIZE / 2;
    private int ballXSpeed = 5, ballYSpeed = 5, player1Score = 0, player2Score = 0, currentSet = 1;
    private boolean isPaused = false, gameOver = false, timeUp = false, inMiniPause = false;
    private Timer timer;
    private boolean player1Up = false, player1Down = false, player2Up = false, player2Down = false;
    private boolean waitingForPlayerStart = false;

    public juegopong() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(200, 200, 0));
        setFocusable(true);
        requestFocus();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) player1Up = true;
                if (e.getKeyCode() == KeyEvent.VK_S) player1Down = true;
                if (e.getKeyCode() == KeyEvent.VK_UP) player2Up = true;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) player2Down = true;
                
                if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
                    resetGame();
                }
                
                if (e.getKeyCode() == KeyEvent.VK_ENTER && inMiniPause && waitingForPlayerStart) {
                    waitingForPlayerStart = false;
                    endMiniPause();
                }

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> player1Up = false;
                    case KeyEvent.VK_S -> player1Down = false;
                    case KeyEvent.VK_UP -> player2Up = false;
                    case KeyEvent.VK_DOWN -> player2Down = false;
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
        g.fillRect((WIDTH - COURT_WIDTH) / 2 - PADDLE_WIDTH, player1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect((WIDTH + COURT_WIDTH) / 2, player2Y, PADDLE_WIDTH, PADDLE_HEIGHT);

        g.setColor(Color.BLACK);
        int lineHeight = PADDLE_HEIGHT / 4;
        g.fillRect((WIDTH + COURT_WIDTH) / 2, player2Y + lineHeight, PADDLE_WIDTH, 1);
        g.fillRect((WIDTH + COURT_WIDTH) / 2, player2Y + 2 * lineHeight, PADDLE_WIDTH, 1);
        g.fillRect((WIDTH + COURT_WIDTH) / 2, player2Y + 3 * lineHeight, PADDLE_WIDTH, 1);

        g.setColor(Color.BLACK);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        if (currentSet == 1) {
            g.drawString("Jugador 1: " + player1Score, 20, 30);
            g.drawString("Jugador 2: " + player2Score, WIDTH - 150, 30);
        } else {
            
            g.drawString("Jugador 1: " + player1Score, WIDTH - 150, 30);
            g.drawString("Jugador 2: " + player2Score, 20, 30);
        }

        g.drawString("Tiempo restante: " + (remainingTime / 1000) + "s", WIDTH / 2 - 60, 30);

        if (isPaused) drawPauseMenu(g);
        if (gameOver) drawGameOverScreen(g);
        if (inMiniPause) drawMiniPauseScreen(g);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused && !gameOver && !inMiniPause && !waitingForPlayerStart) {
            movePaddles();
            moveBall();
            checkCollisions();
            checkWin();

            long currentTime = System.currentTimeMillis();
            remainingTime -= currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;

            if (remainingTime <= 0) {
                handleTimeUp();
            }

            repaint();
        } else if (inMiniPause && waitingForPlayerStart) {
            repaint();
        }
    }


    private void movePaddles() {
        if (currentSet == 1) {
            if (player1Up && player1Y > (HEIGHT - COURT_HEIGHT) / 2) player1Y -= 10;
            if (player1Down && player1Y < (HEIGHT - COURT_HEIGHT) / 2 + COURT_HEIGHT - PADDLE_HEIGHT) player1Y += 10;
            if (player2Up && player2Y > (HEIGHT - COURT_HEIGHT) / 2) player2Y -= 10;
            if (player2Down && player2Y < (HEIGHT - COURT_HEIGHT) / 2 + COURT_HEIGHT - PADDLE_HEIGHT) player2Y += 10;
        } else {
            if (player2Up && player1Y > (HEIGHT - COURT_HEIGHT) / 2) player1Y -= 10;
            if (player2Down && player1Y < (HEIGHT - COURT_HEIGHT) / 2 + COURT_HEIGHT - PADDLE_HEIGHT) player1Y += 10;
            if (player1Up && player2Y > (HEIGHT - COURT_HEIGHT) / 2) player2Y -= 10;
            if (player1Down && player2Y < (HEIGHT - COURT_HEIGHT) / 2 + COURT_HEIGHT - PADDLE_HEIGHT) player2Y += 10;
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

        if (ballX <= x + PADDLE_WIDTH && ballY + BALL_SIZE > player1Y && ballY < player1Y + PADDLE_HEIGHT) {
            ballXSpeed = -ballXSpeed;
            ballX = x + PADDLE_WIDTH;
            if (Math.abs(ballXSpeed) < MAX_BALL_SPEED) ballXSpeed *= SPEED_INCREMENT;
            if (Math.abs(ballYSpeed) < MAX_BALL_SPEED) ballYSpeed *= SPEED_INCREMENT;
        }

        if (ballX + BALL_SIZE >= x + COURT_WIDTH - PADDLE_WIDTH && ballY + BALL_SIZE > player2Y && ballY < player2Y + PADDLE_HEIGHT) {
            ballXSpeed = -ballXSpeed;
            ballX = x + COURT_WIDTH - PADDLE_WIDTH - BALL_SIZE;
            if (Math.abs(ballXSpeed) < MAX_BALL_SPEED) ballXSpeed *= SPEED_INCREMENT;
            if (Math.abs(ballYSpeed) < MAX_BALL_SPEED) ballYSpeed *= SPEED_INCREMENT;
        }

        if (currentSet == 1) {
            if (ballX < x - BALL_SIZE) {
                player2Score++;
                resetBall();
                if (player2Score >= POINTS_TO_WIN || player1Score >= POINTS_TO_WIN) checkWin();
            }

            if (ballX > x + COURT_WIDTH) {
                player1Score++;
                resetBall();
                if (player1Score >= POINTS_TO_WIN || player2Score >= POINTS_TO_WIN) checkWin();
            }
        } else {
            if (ballX < x - BALL_SIZE) {
                player1Score++; 
                resetBall();
                if (player1Score >= POINTS_TO_WIN || player2Score >= POINTS_TO_WIN) checkWin();
            }

            if (ballX > x + COURT_WIDTH) {
                player2Score++; 
                resetBall();
                if (player2Score >= POINTS_TO_WIN || player1Score >= POINTS_TO_WIN) checkWin();
            }
        }
    }

    private void checkWin() {
        if (Math.abs(player1Score - player2Score) >= 2 && (player1Score >= POINTS_TO_WIN || player2Score >= POINTS_TO_WIN)) {
            handleGameEnd();
        }
    }

    private void handleTimeUp() {
        timeUp = true;
        timer.stop();

        if (currentSet == 2) {
            if (player1Score == player2Score) {
                gameOver = true;
            } else {
                handleGameEnd();
            }
        } else {
            inMiniPause = true;
            waitingForPlayerStart = true;
            miniPauseStartTime = System.currentTimeMillis();
        }
    }

    private void handleGameEnd() {
        if (Math.abs(player1Score - player2Score) >= 2 && (player1Score >= POINTS_TO_WIN || player2Score >= POINTS_TO_WIN)) {
            gameOver = true;
            timer.stop();
        }
    }

    private void resetBall() {
        ballX = COURT_WIDTH / 2 - BALL_SIZE / 2;
        ballY = COURT_HEIGHT / 2 - BALL_SIZE / 2;
        ballXSpeed = 5;
        ballYSpeed = 5;
    }

    private void resetGame() {
        player1Score = 0;
        player2Score = 0;
        currentSet = 1;
        gameOver = false;
        timeUp = false;
        inMiniPause = false;
        remainingTime = GAME_TIME;
        resetBall();
        timer.start();
        repaint();
    }

    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String message = "PAUSADO";
        FontMetrics metrics = g.getFontMetrics();
        int x = (WIDTH - metrics.stringWidth(message)) / 2;
        int y = HEIGHT / 2 - metrics.getHeight() / 2;
        g.drawString(message, x, y);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String subMessage = "Presiona ESC o P para reanudar";
        metrics = g.getFontMetrics();
        x = (WIDTH - metrics.stringWidth(subMessage)) / 2;
        y += metrics.getHeight() + 10; 
        g.drawString(subMessage, x, y);
    }


    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        
        String resultMessage;
        if (player1Score == player2Score && currentSet == 2) {
            resultMessage = "EMPATE";
        } else if (player1Score > player2Score) {
            resultMessage = "¡JUGADOR 1 GANA!";
        } else {
            resultMessage = "¡JUGADOR 2 GANA!";
        }
        
        FontMetrics metrics = g.getFontMetrics();
        int x = (WIDTH - metrics.stringWidth(resultMessage)) / 2;
        int y = HEIGHT / 2 - metrics.getHeight() / 2;
        g.drawString(resultMessage, x, y);
        
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String subMessage = "Presiona ENTER para reiniciar";
        metrics = g.getFontMetrics();
        x = (WIDTH - metrics.stringWidth(subMessage)) / 2;
        y += metrics.getHeight() + 10; 
        g.drawString(subMessage, x, y);
    }

    private void drawMiniPauseScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String message = "ENTRETIEMPO";
        FontMetrics metrics = g.getFontMetrics();
        int x = (WIDTH - metrics.stringWidth(message)) / 2;
        int y = HEIGHT / 2 - metrics.getHeight() / 2;
        g.drawString(message, x, y);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String subMessage = "Reanuda el juego presionando ENTER";
        metrics = g.getFontMetrics();
        x = (WIDTH - metrics.stringWidth(subMessage)) / 2;
        y += metrics.getHeight() + 10; 
        g.drawString(subMessage, x, y);
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
            lastUpdateTime = System.currentTimeMillis();

        }
        repaint();
    }

    private void endMiniPause() {
        inMiniPause = false;
        remainingTime = GAME_TIME;
        currentSet++;
        resetBall();
        timer.start();
        repaint();
        lastUpdateTime = System.currentTimeMillis();

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong");
        juegopong game = new juegopong();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }  
}

