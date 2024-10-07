package com.example;

import java.util.Scanner;
import java.util.function.Function;

import javax.swing.*;

import java.awt.*;
// import java.awt.event.KeyListener;
// import java.awt.event.KeyEvent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class App 
{
    public static XYSeries series;
    public static JFreeChart chart;
    public static ChartPanel chartPanel;
    public static 
    Function<Double, Double> func = x -> Math.pow(x, 2) - 4; // setting the equation 

    public static double method(Function<Double, Double> func, double leftPoint, double rightPoint, double accuracy, int[] count){
        double funcLeftPoint = func.apply(leftPoint); //calculating value at the left point
        double funcRightPoint = func.apply(rightPoint); // calculating value at the right point
        double secondDer = secondDerivative(leftPoint);


        if(funcLeftPoint*funcRightPoint >= 0){ // checking the existance of a root of an equation
            throw new IllegalArgumentException("Знаки правой и левой границы должны быть различны!");
        }

        double newPoint  = 0;

        if(funcLeftPoint*secondDer>0){ // graph type check
            while(true){
                newPoint = rightPoint - (funcRightPoint*(rightPoint - leftPoint))/(funcRightPoint - funcLeftPoint); // using the formula for a new point
                double funcNewPoint = func.apply(newPoint); // calculating value at the new point
                drawLine(leftPoint, func.apply(leftPoint), newPoint, funcNewPoint);

                count[0]++;

                if(Math.abs(rightPoint-newPoint)<accuracy){ // root check
                    return newPoint;
                }

                rightPoint = newPoint; // mooving the boundary of the graph
                funcRightPoint = funcNewPoint;
            }
        }else{
            while(true){
                newPoint = leftPoint - (funcLeftPoint*(rightPoint - leftPoint))/(funcRightPoint-funcLeftPoint);
                double funcNewPoint = func.apply(newPoint);
                drawLine(rightPoint, func.apply(rightPoint), newPoint, funcNewPoint);

                count[0]++;

                if(Math.abs(newPoint - leftPoint)<accuracy){
                    return newPoint;
                }

                leftPoint = newPoint;
                funcLeftPoint = funcNewPoint;
            }
        }
    }

    public static void drawLine(double x1, double y1, double x2, double y2){
        try {
            XYPlot plot = (XYPlot) chart.getPlot();
            XYLineAnnotation line = new XYLineAnnotation(x1, y1, x2, y2, new BasicStroke(0.3f), Color.RED);
            plot.addAnnotation(line);

            Thread.sleep(500);//delay for rendering graph

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int[] count = {0};
        System.out.println("Enter leftPoint, rightPoint and accuracy: ");
        int leftPoint = scan.nextInt();
        int rightPoint = scan.nextInt();
        double accuracy = scan.nextDouble();
        // Создаем серию данных
        series = new XYSeries("Chord method");

        // Добавляем данные в серию (например, от 0 до 10)
        for (double x = leftPoint; x <= rightPoint; x += 0.1) {
            double y = func.apply(x);
            series.add(x, y);
        }

        // Создаем набор данных
        XYDataset dataset = new XYSeriesCollection(series);

        // Создаем график
        chart = ChartFactory.createXYLineChart(
                "График функции", // Заголовок
                "x",                          // Ось X
                "y",                          // Ось Y
                dataset,                      // Набор данных
                PlotOrientation.VERTICAL,
                true,                         // Легенда
                true,                         // Подсказки
                false                         // URL-адреса
        );

        // Создаем панель для отображения графика
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));

        // Создаем окно и добавляем панель с графиком
        JFrame frame = new JFrame("График функции");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);


        double valueAtTheRoot = method(func, leftPoint, rightPoint, accuracy, count);
        System.out.println("The root at the x = " + valueAtTheRoot);
        System.out.println("At this point the function takes the value = " + func.apply(valueAtTheRoot));
        System.out.println("Achived in " + count[0] + " iterations");
        scan.close();
    }
}