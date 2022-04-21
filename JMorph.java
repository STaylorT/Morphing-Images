import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JFileChooser;
import java.io.File;

public class Jmorph extends JFrame implements Runnable{
    // Instance variables
    private BufferedImage image;   // the image
    private MyImageObj image1;      // a component in which to display an image
    private MyImageObj image2;


    JFrame newWindow = new JFrame();
    private MyImageObj outputImage;

    private int NUM_COLS = 5;
    private int NUM_ROWS = 5;

    private Container c;
    private JSlider framesSlider;
    private int numFrames = 10;
    private JLabel framesLabel;
    private JButton resetButton;
    private JButton startButton;

    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;

    private int changedPoint = -1;

    // which handle is dragged
    int drag = -1;

    JPanel imageHolder;

    public Jmorph() {
        super();
        this.buildComponents();
        this.buildDisplay();
    }


    private void selectFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileType = selectedFile.toString().substring(selectedFile.toString().length()-3);
            fileType = fileType.toLowerCase();
            System.out.println("file type:" + fileType);

            if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("gif") || fileType.equals("peg")) {
                System.out.println("Valid file type.");
                image1.setImage(readImage(selectedFile.toString()));
                image2.setImage(readImage(selectedFile.toString()));
                outputImage.setImage(readImage(selectedFile.toString()));
                image1.setGrid();
                image2.setGrid();
                outputImage.setGrid();
                repaint();
            }
            else{
                System.out.println("File must be png, jpg, gif, or jpeg.");
            }
            System.out.println("Selected file: " + selectedFile );
        }
    }

    private void buildComponents(){
        // read in image
        image1 = new MyImageObj(readImage("boat.jpg"));
        image2 = new MyImageObj(readImage("boat.jpg"));
        outputImage = new MyImageObj(readImage("boat.jpg"));

        // create menu
        menuBar = new JMenuBar();
        menu = new JMenu("Menu");
        menuBar.add(menu);
        menuItem = new JMenuItem("Upload Image");
        menuItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        selectFile();
                    }
                }
        );
        menu.add(menuItem);
        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(1);
                    }
                }
        );
        menu.add(menuItem);

        // show number of frames selected
        framesLabel = new JLabel("5 frames");

        // get slider to control rotation transformation
        framesSlider = new JSlider(SwingConstants.HORIZONTAL,10,100,20);
        framesSlider.setMajorTickSpacing(10);
        framesSlider.setPaintTicks(true);

        framesSlider.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        framesLabel.setText(framesSlider.getValue() + " frames");
                        numFrames = framesSlider.getValue();
                    }
                }
        );

        // create reset button
        resetButton = new JButton("Reset");
        resetButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        image1.setGrid();
                        image2.setGrid();
                        framesSlider.setValue(20);
                        framesLabel.setText("20 frames");
                    }
                }
        );
        // create start button
        startButton = new JButton("Start");
        startButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        applyWarp();
                    }
                }
        );

    }

    private void applyWarp(){
        // get new window
        framesLabel.setText("Starting animation");
        newWindow = new JFrame();
        newWindow.pack();
        newWindow.setSize(outputImage.getPreferredSize());
        newWindow.setVisible(true);
        outputImage.setGrid();
        newWindow.add(outputImage);
        (new Thread(this)).start();


    }
    public void run()
    {
        try {// begin transformation
            for (int i = 0; i< numFrames ;i++) {
                int[] temp_xs = outputImage.getXs();
                int[] temp_ys = outputImage.getYs();
                for (int j = 0; j < NUM_ROWS * NUM_ROWS; j++) {
                    int x_step = (int) (image2.getXs()[j] - image1.getXs()[j]) / (numFrames);
                    int y_step = (int) (image2.getYs()[j] - image1.getYs()[j]) / (numFrames);
                    temp_xs[j] += x_step;
                    temp_ys[j] += y_step;
                }
                newWindow.repaint();
                Thread.sleep(50);
            }
        } catch (Exception e) {}
    }

    private void buildDisplay(){
        // Control JPanel (1)
        JPanel controlPanel = new JPanel();
        //
        GridLayout grid = new GridLayout(1, 2, 5, 0);
        imageHolder = new JPanel();
        imageHolder.setLayout(grid);
        imageHolder.add(image1);
        imageHolder.add(image2);
        imageHolder.setPreferredSize(new Dimension(2*(int)image1.getPreferredSize().getWidth(),(int)image1.getPreferredSize().getHeight() ));

        image1.setGrid();
        image2.setGrid();
        outputImage.setGrid();

        controlPanel.add(framesSlider);
        controlPanel.add(framesLabel);
        controlPanel.add(resetButton);
        controlPanel.add(startButton);

        //
        c = this.getContentPane();
        this.add(menuBar, BorderLayout.PAGE_START);
        this.add(imageHolder, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.PAGE_END);

        this.setPreferredSize(new Dimension(2*(int)image1.getPreferredSize().getWidth(), 100 +(int)image1.getPreferredSize().getHeight()));
        this.setVisible(true);

    }

    // This method reads an Image object from a file indicated by
    // the string provided as the parameter.  The image is converted
    // here to a BufferedImage object, and that new object is the returned
    // value of this method.
    // The mediatracker in this method can throw an exception

    public BufferedImage readImage (String file) {

        Image image = Toolkit.getDefaultToolkit().getImage(file);
        MediaTracker tracker = new MediaTracker (new Component () {});
        tracker.addImage(image, 0);
        try { tracker.waitForID (0); }
        catch (InterruptedException e) {}
        BufferedImage bim = new BufferedImage
                (image.getWidth(this), image.getHeight(this),
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bim.createGraphics();
        big.drawImage (image, 0, 0, this);
        return bim;
    }

    // The main method allocates the "window" and makes it visible.
    // The windowclosing event is handled by an anonymous inner (adapter)
    // class
    // Command line arguments are ignored.

    public static void main(String[] argv) {

        JFrame frame = new Jmorph();
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener (
                new WindowAdapter () {
                    public void windowClosing ( WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }


    public class MyImageObj extends JLabel {

        // instance variable to hold the buffered image
        private BufferedImage bim=null;
        private BufferedImage filteredbim=null;

        private boolean showfiltered = false;

        static int highlightPoint = -1;
        // Default constructor
        public MyImageObj() {
        }

        //draggable point handles
        Rectangle handles[] = new Rectangle[25];

        private int xs[],
                ys[];

        // This constructor stores a buffered image passed in as a parameter
        public MyImageObj(BufferedImage img) {
            xs = new int[25];
            ys = new int[25];
            bim = img;
            filteredbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_INT_RGB);
            setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));

            this.repaint();

            addMouseListener(new MouseAdapter() {
                private Color background;

                @Override
                public void mousePressed(MouseEvent e) {
                    startDrag(e.getX(), e.getY());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    endDrag();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    doDrag(e.getX(), e.getY());
                }

            });
        }

        // This mutator changes the image by resetting what is stored
        // The input parameter img is the new image;  it gets stored as an
        //     instance variable
        public void setImage(BufferedImage img) {
            if (img == null) return;
            bim = img;
            filteredbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_INT_RGB);
            setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));
            showfiltered=false;
            this.repaint();
        }


        // accessor to get a handle to the bufferedimage object stored here
        public BufferedImage getImage() {
            return bim;
        }


        public void ApplyAffine(int rot) {
            float radianrot = (float)((double)rot/(double)180.0*(double)(Math.PI));
            float x= (float)bim.getWidth()/(float)2.0;
            float y= (float)bim.getHeight()/(float)2.0;

            if (bim == null) return;
            AffineTransform transform = new
                    AffineTransform(Math.cos(radianrot), Math.sin(radianrot),
                    -Math.sin(radianrot), Math.cos(radianrot),
                    x-x*Math.cos(radianrot)+y*Math.sin(radianrot),
                    y-x*Math.sin(radianrot)-y*Math.cos(radianrot));
            //AffineTransform transform = new
            //AffineTransform(Math.cos(radianrot), Math.sin(radianrot),
            //-Math.sin(radianrot), Math.cos(radianrot),
            //0.0, 0.0);
            //AffineTransform transform = new
            //AffineTransform(1.0, 4.3, -1.1, 1.0,
            //8.0, -10.0);


            Graphics2D g2 = filteredbim.createGraphics();
            g2.fillRect(0, 0, (int)x*2,(int)y*2);
            g2.setTransform(transform);
            g2.setColor(new Color(0,0,0));
            g2.drawImage(bim, 0, 0, null);
            g2.dispose();

            showfiltered=true;
            this.repaint();
        }

        //  show current image by a scheduled call to paint()
        public void showImage() {
            if (bim == null) return;
            showfiltered=false;
            this.repaint();
        }

        // offset points on grid so the grid lies within the image boundaries
        private int getOffset(int dim, int gap, int numSections){
            int offset = (dim - gap*(numSections-1))/2;
            return offset;
        }
        /* store starting point, which point is dragged */
        public void startDrag(int x, int y)
        {
            /* find which handle if any is trying to be dragged */
            for (int i = 0; i < NUM_ROWS*NUM_COLS; i++) {
                if (handles[i].contains(x, y)) {
                    drag = i;
                    highlightPoint = drag;
                    return;
                }
            }
        }

        /* move the handle and repaint */
        public void doDrag(int x, int y)
        {
            /* only if a handle is being dragged */
            if (drag > -1) {
                xs[drag] = x;
                ys[drag] = y;
                handles[drag].setRect(xs[drag]-5, ys[drag]-5, 10, 10);
                repaint();
            }
        }
        public void endDrag()
        {
            resetHandles();
            image2.resetHandles();
        }
        private void resetHandles(){
            highlightPoint = -1;
            for (int i = 0; i<NUM_ROWS*NUM_COLS; i++){
                handles[i].setSize(10, 10);
            }
            repaint();
        }

        private Rectangle[] getHandles(){
            return handles;
        }
        private int[] getXs(){
            return xs;
        }
        private int[] getYs(){
            return ys;
        }
        private void setXs(int[] new_xs){
            xs = new_xs;
        }
        private void setYs(int[] new_ys){
            ys = new_ys;
        }

        // setting regular row x cols grid
        private void setGrid(){
            int width = (int) image1.getPreferredSize().getWidth();
            int height = (int) image1.getPreferredSize().getHeight();
            int x_gap = width/(NUM_COLS-1);
            int y_gap = height/(NUM_ROWS-1);

            int col = 0;
            for (int i = 1; i < NUM_ROWS*NUM_COLS; i++) {
                xs[i] = i%NUM_COLS*x_gap;
                ys[i] = col%NUM_ROWS*y_gap;
                if (i%NUM_COLS == NUM_COLS-1)
                    col++;
                handles[i] = new Rectangle(xs[i], ys[i], 10, 10);
            }

            handles[0] = new Rectangle(xs[0], ys[0], 10, 10);
            repaint();
        }
        //  get a graphics context and show either filtered image or
        //  regular image
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            // draw image
            g2d.drawImage(bim, 0, 0, this);

            // start drawing grid
            // draw in first point special cases
            g2d.fill(handles[0]);
            g2d.drawLine(xs[0], ys[0], xs[1], ys[1]);
            g2d.drawLine(xs[0], ys[0], xs[NUM_COLS], ys[NUM_ROWS]);
            g2d.drawLine(xs[1], ys[1], xs[2], ys[2]);
            g2d.drawLine(xs[0], ys[0], xs[0 + NUM_COLS +1], ys[0 + NUM_ROWS +1]);

            // draw rest of the grid, looking at special cases
            for (int i = 1 ;i < 25; i++){
                handles[i].setLocation(xs[i], ys[i]);
                if (highlightPoint == i){
                    g2d.setColor(new Color(100, 200, 188));
                    image2.repaint();
                    g2d.fill(handles[i]);
                    handles[i].setSize(16, 16);
                    g2d.setColor(new Color(0,0,0));
                }
                else{
                    g2d.fill(handles[i]);
                }

                if (i % 5 != 0)
                    g2d.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
                if (i < 20)
                    g2d.drawLine(xs[i], ys[i], xs[i + NUM_COLS], ys[i + NUM_ROWS]);
                if (i%5 != 4 && i <20)
                    g2d.drawLine(xs[i], ys[i], xs[i + NUM_COLS +1], ys[i + NUM_ROWS +1]);
            }
        }
    }
}
