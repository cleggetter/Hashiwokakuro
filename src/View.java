import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This is the view component of MVC
 * It runs in the Swing event dispatcher thread,
 * and only depends on the current model state.
 * 
 * This observes the model and reacts to changes
 * 
 * @author legge
 *
 */
public class View implements Runnable {

	private MainFrame mainFrame;
	
	
	private ViewConfig viewConfig;
	
	private class ViewConfig {
		public String name;
		public MainFrame mainFrame;
		public ArrayList<JPanel> panels;
		public ArrayList<JButton> buttons;
		public Model model;
		public InputSignal signals;
		
		public ViewConfig(String name) {
			this.name = name;
			panels = new ArrayList<JPanel> ();
			buttons = new ArrayList<JButton> ();
		}
	}
	
	public View(Model model, InputSignal signals) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
		}
		viewConfig = new ViewConfig("Grid");
		viewConfig.signals = signals;
		viewConfig.model = model;
		ButtonPanel buttons = new ButtonPanel(viewConfig);

		viewConfig.panels.add(new GridPanel(viewConfig));
		viewConfig.panels.add(new ButtonPanel(viewConfig));
		
		viewConfig.mainFrame = new MainFrame(viewConfig);
        model.addObserver(new ModelObserver(this));
    
    }
	
	@Override
	public void run() {
		// Nothing to run after initialization - event driven UI 
		// takes care of all updates
	}
	
	/** 
	 * Observer implementation to react to changes in the model
	 * @author legge
	 *
	 */
	private class ModelObserver implements Observer {
		private View viewer;
		
		ModelObserver(View viewer){
			this.viewer = viewer;
		}
        @Override
        public void update(Observable o, Object arg) {
        	viewer.refresh();
        }
    }
	public void refresh() {
		viewConfig.mainFrame.refresh();
	}
	
	private class MainFrame extends JFrame implements MouseListener{
		ViewConfig viewConfig;
		
		private static final long serialVersionUID = 1L;

		public MainFrame(ViewConfig vconfig) {
			super(vconfig.name);
			viewConfig = vconfig;
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setLayout(new BorderLayout());
			
			addMouseListener(this);

			add(viewConfig.panels.get(0), BorderLayout.CENTER);
			add(viewConfig.panels.get(1),BorderLayout.PAGE_END);
			
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
		
		
		public void refresh() {				
			invalidate();
			repaint();
		}
		
		@Override
	    public void mouseClicked(MouseEvent e) {
			viewConfig.signals.add("MainFrameClicked");
	    	repaint();
	    }
	    @Override
	    public void mouseExited(MouseEvent e) {
	     }
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	
	/**
	 * Generate a panel with the buttons 
	 * @author legge
	 *
	 */
	private class ButtonPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		ArrayList<JButton> buttons;
		ViewConfig viewConfig;
			
		public ButtonPanel(ViewConfig vconfig) {
			viewConfig = vconfig;
			JButton backButton = new JButton("Back");        
			JButton forwardButton = new JButton("Forward");
			JButton exitButton = new JButton("Exit");
			backButton.setHorizontalTextPosition(SwingConstants.LEFT);   
			forwardButton.setHorizontalTextPosition(SwingConstants.RIGHT);  
			exitButton.setHorizontalTextPosition(SwingConstants.CENTER);  

			backButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					viewConfig.signals.add("back");
				}          
			});
			forwardButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					viewConfig.signals.add("forward");
				}          
			});
			exitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					viewConfig.signals.add("exit");
					viewConfig.mainFrame.dispose();
				}          
			});
		
			this.add(backButton);
			this.add(forwardButton);
			this.add(exitButton);
			setPreferredSize(new Dimension(100, 100));
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(100, 100);
		}
	}
	
	/**
	 * Panel for the graph grid display
	 * @author legge
	 *
	 */
	
	private class GridPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private GridDefinition grid;
		private int rowCount, columnCount;
		private int xOffset, yOffset;
		private int cellHeight, cellWidth;
		private int id;
		
		public GridPanel(Model model) {
			
	        super(new BorderLayout());
	        id = UniqueId.getUid();
	        this.grid = model.grid;
	        this.rowCount = grid.getNumRows();
			this.columnCount = grid.getNumCols();
	        this.setVisible(true);
	        //repaint();
	        
	    }	
		public GridPanel(ViewConfig vconfig) {
			
	        super(new BorderLayout());
	        id = UniqueId.getUid();
	        this.grid = vconfig.model.grid;
	        this.rowCount = grid.getNumRows();
			this.columnCount = grid.getNumCols();
	        this.setVisible(true);
	    }	
	
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(800, 800);
		}

		// Top left corner of cell
		private int convertY(int y) {
			return yOffset + (y * cellHeight);
		}

		private int convertX(int x) {
			return xOffset + (x * cellWidth);
		}
	
		// Shape relative x;
		private int xyQuartile(int xy, int widthOrHeight, int quart) {
			return xy + widthOrHeight * quart/4;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
		
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g.create();

			ArrayList<ImageForIsland> images = gatherImages();
			for (ImageForIsland ni : images) {
				if (ni == null) {
					continue;
				}
				g2d.setColor( ni.color());
				g2d.fill(ni.getIslandImage());
				
				//set the stroke of the copy, not the original 
				for (ImageForIsland.LineDef l : ni.getLineDefs()) {
					g2d.setStroke(l.stroke);		
					g2d.setColor( l.color );
					g2d.draw(l.line);
				}
				ArrayList<TextLayout> tlist = ni.getLabels(g2d);
				g2d.setColor(Color.gray);
			    
				int qy = Math.max(3 - tlist.size(), 1);
				int qx = ni.textCenter ? 1 : 0;
				g2d.setColor( Color.BLACK);    
				for (TextLayout t : tlist) {
					t.draw(g2d, xyQuartile(ni.x, ni.height, qx), 
								xyQuartile(ni.y,ni.height,qy++));
				}
			    
			//}
			}
			g2d.dispose();
		}
		
		
		// Generate the image of the graph using the latest model info
		private ArrayList<ImageForIsland> gatherImages()
		{
			ArrayList<ImageForIsland> images = new ArrayList<ImageForIsland>();
		
			int width = getWidth();
			int height = getHeight();

			cellWidth = width / columnCount;
			cellHeight = height / rowCount;

			xOffset = (width - (columnCount * cellWidth)) / 2;
			yOffset = (height - (rowCount * cellHeight)) / 2;
		
			Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
			Stroke dotted = new BasicStroke(3, BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
			Stroke solid = new BasicStroke(3);
		
			for (int row = 0; row < rowCount; row++) {
				for (int col = 0; col < columnCount; col++) {
				// Rectangle cell = new Rectangle(
					ImageForIsland icell = new ImageForIsland(
										convertX(col), convertY(row)
										);
					if (grid.get(row,col).isSolid()) {
						icell.solid(cellWidth /2, cellHeight /2,
								grid.get(row,col).getLabel());
						if (grid.get(row, col).tracker.isSolved()) {
								icell.setColor(Color.CYAN);
						}
					}
					else if (grid.get(row,col).isConstraint()) {
						icell.constraint(cellWidth, cellHeight/2,
								grid.get(row,col).getLabel());
					}
					else {
						continue;
					}
					ArrayList<Bridge> bs = grid.get(row, col).getBridgesOut();
					for (Bridge b : bs ) {
						Color lineColor = b.tracker.isSolved() ? Color.RED : Color.BLACK;
						Stroke style = solid;
					
						ArrayList<Integer> r = new ArrayList<Integer>();
						if (b.tracker.minPossibleScore == 0) {
							if (b.tracker.isSolved()) {
								r.add(2);
								style = dotted;
								lineColor = Color.LIGHT_GRAY;
							}
							else {
								r.add(2);
								style = dashed;
								lineColor = Color.BLACK;
							}
						}
						if (b.tracker.minPossibleScore == 1) {
							r.add(2);
							style = solid;
						}
						if (b.tracker.minPossibleScore == 2) {
							r.add(1);
							r.add(3);
							style = solid;
						}	

						for (int w : r){
							int x1,x2,y1,y2;
							if (b.isHorizontal()) {
								x1 = xyQuartile(convertX(b.fromIsland.col), icell.width, 4);
								x2 = xyQuartile(convertX(b.toIsland.col), icell.width, 0);
								y1 = xyQuartile(convertY(b.fromIsland.row), icell.height, w);
								y2 = xyQuartile(convertY(b.toIsland.row), icell.height, w);
							}
							else {
								x1 = xyQuartile(convertX(b.fromIsland.col), icell.width, w);
								x2 = xyQuartile(convertX(b.toIsland.col), icell.width, w);
								y1 = xyQuartile(convertY(b.fromIsland.row), icell.height, 4);
								y2 = xyQuartile(convertY(b.toIsland.row), icell.height, 0);
							}
						
							icell.addLine( x1,y1,x2,y2, lineColor, style);
						
						}
					}
				
					icell.addLabel(grid.get(row, col).getLabel(), grid.get(row,col).isSolid());			
					images.add(icell);
				}
			}
			return images;
		}
	}
	
}
