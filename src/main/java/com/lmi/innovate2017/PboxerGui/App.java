package com.lmi.innovate2017.PboxerGui;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class App extends Application implements Runnable
{
	//final Path inputFilePath = Paths.get("C:\\Users\\xil\\Desktop\\input.txt");
	//final Path outputFilePath = Paths.get("C:\\Users\\xil\\Desktop\\output.txt");
	//final Path inputFilePath = Paths.get("C:\\stash\\AUDIO\\pbox\\controller.txt");
	Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"));
	final Path inputFilePath = Paths.get(tempPath.toString(), "pbox", "controller.txt");
	final Path outputFilePath = Paths.get(tempPath.toString(), "pbox", "score.txt");
	
	private final StringProperty measureDistortion = new SimpleStringProperty();
	private final StringProperty measureClicking = new SimpleStringProperty();
	
	private final StringProperty overallMos = new SimpleStringProperty();
	private final StringProperty distortionMos = new SimpleStringProperty();
	private final StringProperty clickingMos = new SimpleStringProperty();	
	
	public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage displayStage) {
    	setControlStage();
        setDisplayStage(displayStage);
    }
    
    private void setControlStage() {
    	// init
    	try {
			FileAccessor.write(inputFilePath, 0, "0");
			FileAccessor.write(inputFilePath, 1, "0");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}    	
    	
    	// set control pane
    	Slider slider1 = new Slider(0, 100, 0);
    	slider1.setShowTickLabels(true);
    	slider1.setShowTickMarks(true);
    	slider1.setMajorTickUnit(50);
    	slider1.setMinorTickCount(5);
    	slider1.setBlockIncrement(10);
    	GridPane.setConstraints(slider1, 1, 1);
    	GridPane.setHgrow(slider1, Priority.ALWAYS);
    	
    	slider1.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    try {
						FileAccessor.write(inputFilePath, 0, Integer.toString(new_val.intValue()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
        });
    	
    	Slider slider2 = new Slider(0, 100, 0);
    	slider2.setShowTickLabels(true);
    	slider2.setShowTickMarks(true);
    	slider2.setMajorTickUnit(50);
    	slider2.setMinorTickCount(5);
    	slider2.setBlockIncrement(10);
    	GridPane.setConstraints(slider2, 1, 2);
    	GridPane.setHgrow(slider2, Priority.ALWAYS);
    	
    	slider2.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    try {
						FileAccessor.write(inputFilePath, 1, Integer.toString(new_val.intValue()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
        });
    	
    	GridPane controllerPane = createPane("Controller");    	
    	controllerPane.getChildren().add(slider1);
    	controllerPane.getChildren().add(slider2);
    	
    	// set separator
    	final Separator separator = new Separator();
    	separator.setOrientation(Orientation.HORIZONTAL);
    	separator.setValignment(VPos.CENTER);
    	separator.setPrefHeight(80);
        GridPane.setConstraints(separator, 2, 2);
        GridPane.setRowSpan(separator, 2);
    	
    	// set monitor pane
        GridPane monitorPane = createPane("Monitor");
        monitorPane.add(createLabel("", measureDistortion), 1, 1);
        monitorPane.add(createLabel("", measureClicking), 1, 2);
    	
    	// set box
        final VBox rootNode = new VBox();
        rootNode.getChildren().addAll(controllerPane, separator, monitorPane);
        
        // init scheduler
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        
        // define scheduler behavior
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                final Runnable runnable = new Runnable() {
                    public void run() {
                    	try {
                    		List<String> measures = FileAccessor.read(inputFilePath);
                    		if (measures != null && measures.size() == 2) {
								measureDistortion.setValue(measures.get(0));
								measureClicking.setValue(measures.get(1));
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
                };
                Platform.runLater(runnable);
            }
        }, 0, 1, TimeUnit.SECONDS);
    	
        // set stage
        final Scene scene = new Scene(rootNode, 650, 600);
    	Stage controlStage = new Stage();
        controlStage.setTitle("Pboxer Controller");
        controlStage.setScene(scene);
        controlStage.show();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void setDisplayStage(Stage displayStage) {
        // bar charts
        final NumberAxis xAxis = new NumberAxis(0, 5, 0.5);
        xAxis.tickLabelFontProperty().set(Font.font(25));
        final CategoryAxis yAxis = new CategoryAxis();
        final BarChart<Number,String> mosChart = new BarChart<Number,String>(xAxis, yAxis);        
        XYChart.Series mosSeries = new XYChart.Series();
        mosSeries.getData().add(new XYChart.Data(0, ""));
        mosChart.getData().add(mosSeries);

        final NumberAxis xAxis2 = new NumberAxis(0, 5, 0.5);
        xAxis2.tickLabelFontProperty().set(Font.font(25));
        final CategoryAxis yAxis2 = new CategoryAxis();
        final BarChart<Number,String> distChart = new BarChart<Number,String>(xAxis2, yAxis2);
        XYChart.Series distSeries = new XYChart.Series();
        distSeries.getData().add(new XYChart.Data(0, ""));
        distChart.getData().add(distSeries);

        final NumberAxis xAxis3 = new NumberAxis(0, 5, 0.5);
        xAxis3.tickLabelFontProperty().set(Font.font(25));
        final CategoryAxis yAxis3 = new CategoryAxis();
        final BarChart<Number,String> clickChart = new BarChart<Number,String>(xAxis3, yAxis3);
        XYChart.Series clickSeries = new XYChart.Series();
        clickSeries.getData().add(new XYChart.Data(0, ""));
        clickChart.getData().add(clickSeries);
        
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent event) {
        		mosSeries.getData().clear();
        		distSeries.getData().clear();
        		clickSeries.getData().clear();
        		mosSeries.getData().add(new XYChart.Data(BigDecimal.valueOf(Double.parseDouble(overallMos.getValue())), ""));
        		distSeries.getData().add(new XYChart.Data(BigDecimal.valueOf(Double.parseDouble(distortionMos.getValue())), ""));
        		clickSeries.getData().add(new XYChart.Data(BigDecimal.valueOf(Double.parseDouble(clickingMos.getValue())), ""));
        	}
        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
        
        // set pane
        GridPane pane = createPane("");
        pane.add(createLabel("", overallMos), 2, 0);
        pane.add(createLabel("", distortionMos), 2, 1);
        pane.add(createLabel("", clickingMos), 2, 2);
        pane.add(mosChart, 1, 0);
        pane.add(distChart, 1, 1);
        pane.add(clickChart, 1, 2);
        
        // set box
        final VBox rootNode = new VBox();
        rootNode.getChildren().add(pane);

        // init scheduler
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        
        // define scheduler behavior
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                final Runnable runnable = new Runnable() {
                    public void run() {
                    	try {							
							List<String> scores = FileAccessor.read(outputFilePath);
							if (scores != null && scores.size() == 3) {
								BigDecimal bd = new BigDecimal(scores.get(0));
								BigDecimal bd_cut = bd.setScale(2, RoundingMode.FLOOR);
				    			overallMos.setValue(bd_cut.toString());
				    			
				    			BigDecimal bd2 = new BigDecimal(scores.get(1));
								BigDecimal bd_cut2 = bd2.setScale(2, RoundingMode.FLOOR);
								distortionMos.setValue(bd_cut2.toString());
								
								BigDecimal bd3 = new BigDecimal(scores.get(2));
								BigDecimal bd_cut3 = bd3.setScale(2, RoundingMode.FLOOR);
								clickingMos.setValue(bd_cut3.toString());								
							} else {
								overallMos.setValue("4.75");
								distortionMos.setValue("4.75");
								clickingMos.setValue("4.75");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
                };
                Platform.runLater(runnable);
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        final Scene scene = new Scene(rootNode);
        displayStage.setMaximized(true);
        displayStage.setTitle("Pboxer GUI");
        displayStage.setScene(scene);
    	displayStage.show();
    }

    private GridPane createPane(String title) {
    	GridPane grid = new GridPane();
    	grid.setHgap(10);
    	
    	if (title.equals("Monitor")) {
    		Text paneTitle = new Text(title);
    		paneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 50));
    		grid.add(paneTitle, 0, 0, 2, 1);
    		grid.setAlignment(Pos.BASELINE_LEFT);
    		grid.add(createLabel("Measure distortion:   ", null), 0, 1);
        	grid.add(createLabel("Measure clicking:   ", null), 0, 2);
        	grid.setVgap(10);
        	grid.setPadding(new Insets(25, 25, 25, 25));
    	} else if (title.equals("Controller")) {
    		Text paneTitle = new Text(title);
    		paneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 50));
    		grid.add(paneTitle, 0, 0, 2, 1);
    		grid.setAlignment(Pos.CENTER);
    		grid.add(createLabel("Distortion: ", null), 0, 1);
        	grid.add(createLabel("Clicking: ", null), 0, 2);
        	grid.setVgap(10);
        	grid.setPadding(new Insets(25, 25, 25, 25));
    	} else {
    		grid.setAlignment(Pos.CENTER);
    		grid.add(createLabel("Audio Quality:   ", null), 0, 0);
    		grid.add(createLabel("Distortion-based Audio Quality:   ", null), 0, 1);
        	grid.add(createLabel("Click-based Audio Quality:   ", null), 0, 2);
    		grid.setVgap(50);
    		grid.setPadding(new Insets(25, 25, 25, 25));
    	}	
    	
    	return grid;
    }
    
    private Label createLabel(String name, StringProperty property) {
    	Label label = new Label(name);
    	label.setFont(Font.font("Tahoma", 50));
    	if (property != null) {
    		label.textProperty().bind(property);
    	}
    	
    	return label;
    }
    
    public void run() {
    }
}
