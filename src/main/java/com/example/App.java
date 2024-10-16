package com.example;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class App 
{
    public static Function<Double, Double> func; // setting the equation 
    public static XYSeries series;
    public static XYDataset dataset;
    public static JFreeChart chart;
    public static ChartPanel chartPanel;
    public static JFrame frame;
    public static int count=0;
    public static JLabel rootLabel;
    public static JLabel valueRootLabel;
    public static JLabel countLabel;
    public static double leftPoint = 0;
    public static double rightPoint = 4;
    public static double accuracy = .0001;
    public static double[] data = new double[]{leftPoint, rightPoint, accuracy};

    // Chord method
    public static void method(Function<Double, Double> func){
        // Using swing utility for assinc compliting the chord method 
        // (have to be because it locks the GUI and does not show the method work with the graph)
        SwingWorker<Double, Double[]> worker = new SwingWorker<Double,Double[]>() {
            double localLeftPoint = leftPoint;
            double localRightPoint = rightPoint;
            @Override
            protected Double doInBackground() throws Exception{
                double funcLeftPoint = func.apply(localLeftPoint); //calculating value at the left point
                double funcRightPoint = func.apply(localRightPoint); // calculating value at the right point
                double secondDer = secondDerivative(localLeftPoint); // calculating the secon derivative


                if(funcLeftPoint*funcRightPoint > 0){ // checking the existance of a root of an equation
                    JOptionPane.showMessageDialog(null, "Move the graph boundaries!", "Error: The root does not exists", JOptionPane.ERROR_MESSAGE);
                }

                double newPoint  = 0;

                if(funcLeftPoint*secondDer>0){ // graph type check
                    while(true){
                        newPoint = localRightPoint - (funcRightPoint*(localRightPoint - localLeftPoint))/(funcRightPoint - funcLeftPoint); // using the formula for a new point
                        double funcNewPoint = func.apply(newPoint); // calculating value at the new point
                        publish(new Double[]{localLeftPoint, func.apply(localLeftPoint), newPoint, funcNewPoint}); // sending the data into process to drow the line

                        Thread.sleep(500);//delay for rendering graph

                        count++; // increasing count of iteratinos

                        // root check
                        if(Math.abs(localRightPoint-newPoint)<accuracy){
                            return newPoint;
                        }

                        // mooving the boundary of the graph
                        localRightPoint = newPoint; 
                        funcRightPoint = funcNewPoint;
                    }
                }else{
                    while(true){
                        newPoint = localLeftPoint - (funcLeftPoint*(localRightPoint - localLeftPoint))/(funcRightPoint-funcLeftPoint);
                        double funcNewPoint = func.apply(newPoint);
                        publish(new Double[]{localRightPoint, func.apply(localRightPoint), newPoint, funcNewPoint});

                        Thread.sleep(500);

                        count++;

                        if(Math.abs(newPoint - localLeftPoint)<accuracy){
                            return newPoint;
                        }

                        localLeftPoint = newPoint;
                        funcLeftPoint = funcNewPoint;
                    }
                }
            }

            @Override
            protected void process(List<Double[]> chunks){
                for(Double[] el : chunks){
                    XYPlot plot = (XYPlot) chart.getPlot();
                    XYLineAnnotation line1 = new XYLineAnnotation(el[2], func.apply(el[2]), el[2], 0, new BasicStroke(0.5f), Color.RED);
                    XYLineAnnotation line2 = new XYLineAnnotation(el[0], el[1], el[2], el[3], new BasicStroke(0.5f), Color.RED);
                    plot.addAnnotation(line1);
                    plot.addAnnotation(line2);
                    rootLabel.setText("The temporary root is: " + el[2]);
                }
            }

            @Override
            protected void done(){
                try {
                    double root = get();
                    rootLabel.setText("The root at the x = " + root);
                    valueRootLabel.setText("At this point the function takes the value = " + func.apply(root));
                    countLabel.setText("Achived in " + count + " iterations");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // Первая производная f'(x)
    public static double firstDerivative(double x) {
        double h = 1e-5; // Маленькое значение для численного дифференцирования
        return (func.apply(x + h) - func.apply(x - h)) / (2 * h); // Центральная разностная схема
    }

    // Вторая производная f''(x)
    public static double secondDerivative(double x) {
        double h = 1e-5; // Маленькое значение для численного дифференцирования
        return (firstDerivative(x + h) - firstDerivative(x - h)) / (2 * h); // Центральная разностная схема
    }

    // Creating the second window for graph and method
    public static void showGraphWMethod(double leftPoint, double rightPoint, double accuracy){
        // Добавляем точки графика 
        for (double x = leftPoint; x <= rightPoint; x += 0.1) {
            double y = func.apply(x);
            series.add(x, y);
        }
        
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAnnotation line = new XYLineAnnotation(leftPoint, 0, rightPoint, 0, new BasicStroke(1), Color.BLACK);
        plot.addAnnotation(line);
    }

    public static void createGraphPanel(){
        // Создаем серию данных
        series = new XYSeries("Equation graph");

        // Создаем набор данных
        dataset = new XYSeriesCollection(series);

        // Создаем график
        chart = ChartFactory.createXYLineChart(
                "Equation graph",             // Заголовок
                "x",                          // Ось X
                "y",                          // Ось Y
                dataset,                      // Набор данных
                PlotOrientation.VERTICAL,
                true,                         // Легенда
                true,                         // Подсказки
                false                         // URL-адреса
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        // Создаем панель для отображения графика
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1100, 770));

        // Создаем окно и добавляем панель с графиком и кнопку
        JPanel panel = new JPanel();
        panel.add(chartPanel, BorderLayout.CENTER);
        frame.add(panel);
    }

    public static void main(String[] args) {
        //Setting the frame
        frame = new JFrame("Find the root");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new FlowLayout());

        // Info panel
        JPanel greatPanel = new JPanel();
        greatPanel.setLayout(new GridLayout(6, 1));

        // Creating the methods buttons
        JRadioButton firstGraphButt = new JRadioButton("Equation y = x^2 - 4"); // [-4, 0] && [0, 5] 
        JRadioButton secondGraphButt = new JRadioButton("Equation y =  x^3 + x^2"); // [-1.5. -0.5] && [-0.5, 0.5]

        JTextField[] textFields = new JTextField[3];
        JLabel[] labelFields = new JLabel[3];
        String[] fieldsLabels = {"Left graph side:", "Right graph side:", "Accuracy:"};
        // Creating text fields for data
        for(int i=0; i<3; i++){
            textFields[i] = new JTextField(Double.toString(data[i]), 8);
            labelFields[i] = new JLabel(fieldsLabels[i]);
        }

        // Creating the data senging button
        JButton sendDataButt = new JButton("Send the data");
        sendDataButt.setSize(50, 20);

        // Creating the create graph button
        JButton showGraphButt = new JButton("Show the choosen equation graph");
        showGraphButt.setSize(50, 20);

        // Collecting the methods buttons intonthe radio buttons group
        ButtonGroup graphsButtonGroup = new ButtonGroup();
        graphsButtonGroup.add(firstGraphButt);
        graphsButtonGroup.add(secondGraphButt);

        firstGraphButt.setSelected(true);

        JPanel radioButtonsPanel = new JPanel();
        radioButtonsPanel.setLayout(new GridLayout(2,1));
        JLabel eqnsLabel = new JLabel("Select a function to find its root!");
        eqnsLabel.setFont(new Font("Arial",Font.PLAIN, 20));
        JPanel n1Panel = new JPanel();
        JPanel n2Panel = new JPanel();
        n1Panel.add(eqnsLabel);
        n2Panel.add(firstGraphButt);
        n2Panel.add(secondGraphButt);
        radioButtonsPanel.add(n1Panel);
        radioButtonsPanel.add(n2Panel);

        JPanel buttonsPanel  = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(sendDataButt);
        buttonsPanel.add(showGraphButt);

        // Creating labels for results
        rootLabel = new JLabel("Root is being found...");
        valueRootLabel = new JLabel();
        countLabel = new JLabel();

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(3, 1));
        labelPanel.setPreferredSize(new Dimension(400, 60));
        labelPanel.add(rootLabel);
        labelPanel.add(valueRootLabel);
        labelPanel.add(countLabel);
        labelPanel.setBackground(Color.WHITE);

        // creating border and padding for label panel
        Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
        Border padding = BorderFactory.createEmptyBorder(4, 10, 10, 4);
        Border compoundBorder = BorderFactory.createCompoundBorder(border, padding);
        labelPanel.setBorder(compoundBorder);

        JPanel fieldsPanel = new JPanel();
        for(int i=0; i<3; i++){
            JPanel tempPanel = new JPanel();
            tempPanel.setLayout(new GridLayout(2, 1));;
            tempPanel.add(labelFields[i]);
            tempPanel.add(textFields[i]);
            fieldsPanel.add(tempPanel);
        }        

        // Создаем кнопку для вызова метода
        JButton methodButt = new JButton("Use the chord method");
        methodButt.setSize(50, 20);
        // Добавляем на нее обработчик событий
        methodButt.addActionListener(e -> {
            valueRootLabel.setText(null);
            countLabel.setText(null);
            method(func);
        });
        JPanel methodPanel = new JPanel();
        methodPanel.add(methodButt);

        // Adding all of the buttons and text fields into the frame
        greatPanel.add(radioButtonsPanel);
        greatPanel.add(fieldsPanel);
        greatPanel.add(buttonsPanel);
        greatPanel.add(labelPanel);
        greatPanel.add(methodPanel);
        frame.add(greatPanel);
        // disable graph button because we have no data for it
        showGraphButt.setEnabled(false);
        methodButt.setEnabled(false);


        // sending the data for graph and method and validate it
        sendDataButt.addActionListener(e -> {
            series.clear();
            XYPlot plot = (XYPlot) chart.getPlot();
            plot.getAnnotations().clear();
            chart.fireChartChanged();
            methodButt.setEnabled(false);
            for(int i=0; i<3; i++){
                String input = textFields[i].getText();
                try {
                    data[i] = Double.parseDouble(input);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Error: '" + input + 
                    "' is not the number!", "Input error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
            leftPoint = data[0];
            rightPoint = data[1];
            accuracy = data[2];
            showGraphButt.setEnabled(true);
        });

        // Creating a panel for equation graph
        createGraphPanel();

        // drawing the graph
        showGraphButt.addActionListener(e -> {
            func = firstGraphButt.isSelected() ? x -> Math.pow(x, 2) - 4 : x -> Math.pow(x, 3) + Math.pow(x, 2);
            showGraphWMethod(leftPoint, rightPoint, accuracy);
            methodButt.setEnabled(true);
        });

        // Set frame to visible
        frame.setVisible(true);
    }
}