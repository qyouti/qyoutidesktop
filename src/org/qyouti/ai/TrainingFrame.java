/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TrainingFrame.java
 *
 * Created on 13-Jun-2012, 11:39:38
 */
package org.qyouti.ai;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.learning.IterativeLearning;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.core.transfer.Linear;
import org.neuroph.core.transfer.Sigmoid;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.comp.InputNeuron;
import org.neuroph.nnet.comp.ThresholdNeuron;
import org.neuroph.nnet.flat.FlatNetworkLearning;
import org.neuroph.nnet.flat.FlatNetworkPlugin;
import org.neuroph.nnet.learning.BinaryDeltaRule;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.PerceptronLearning;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.LayerFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import org.qyouti.scan.image.FastFourierTransform2D;
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class TrainingFrame
        extends javax.swing.JFrame
        implements Observer
{
  public static final int imagedisplaywidth = 128;

  private org.apache.batik.swing.JSVGCanvas previewcanvas;
  TrainingSetGenerator gen=null;
  TrainingVectorImage image;
  NeuralNetwork neuralnet;
  LMS learning;

  /** Creates new form TrainingFrame */
  public TrainingFrame()
  {
    int i, j;

    initComponents();
    previewcanvas = new org.apache.batik.swing.JSVGCanvas();
    previewcanvas.setEnableRotateInteractor( false );
    previewcanvas.setName( "previewcanvas" ); // NOI18N
    previewcanvas.setRecenterOnResize( false );
    svgpanel.add( previewcanvas, java.awt.BorderLayout.CENTER );
    pack();

    createNeuralNetwork(
            TrainingSetGenerator.DIMENSION*TrainingSetGenerator.DIMENSION,
            TrainingSetGenerator.DIMENSION*TrainingSetGenerator.DIMENSION  );

  }

  private void createNeuralNetwork( int inputNeuronsCount, int outputNeuronsCount )
  {
    Linear slope = new Linear();
    System.out.println( "Linear transfer function  f(0.5) = " + slope.getOutput( 0.5 ) );
    System.out.println( "Linear transfer function f'(0.5) = " + slope.getDerivative( 0.5 ) );


    int i, j;
    neuralnet = new NeuralNetwork();
    // init neuron settings for input layer
    NeuronProperties inputNeuronProperties = new NeuronProperties();
    inputNeuronProperties.setProperty( "transferFunction", TransferFunctionType.LINEAR );

    // create input layer
    Layer inputLayer = LayerFactory.createLayer( inputNeuronsCount, inputNeuronProperties );
    neuralnet.addLayer( inputLayer );

    NeuronProperties outputNeuronProperties = new NeuronProperties();
    outputNeuronProperties.setProperty( "neuronType", Neuron.class );
    //outputNeuronProperties.setProperty( "thresh", new Double( Math.abs( Math.random() ) ) );
    outputNeuronProperties.setProperty( "transferFunction", TransferFunctionType.LINEAR );
    // for sigmoid and tanh transfer functions set slope propery
    outputNeuronProperties.setProperty( "transferFunction.slope", new Double( 1 ) );

    // createLayer output layer
    Layer outputLayer = LayerFactory.createLayer( outputNeuronsCount, outputNeuronProperties );
    neuralnet.addLayer( outputLayer );

    // create full conectivity between input and output layer
    ConnectionFactory.forwardConnect( inputLayer, outputLayer );
    //ConnectionFactory.fullConnect( inputLayer, outputLayer );

    // set input and output cells for this network
    NeuralNetworkFactory.setDefaultIO( neuralnet );

    learning = new LMS();
    learning.setLearningRate( 0.002 );
    learning.setMaxIterations( 1000 );
    neuralnet.setLearningRule( learning );
    neuralnet.randomizeWeights( 0.01, 0.02 );

    neuralnet.save( "/home/jon/testnet.nnet" );
    // set appropriate learning rule for this network
//		if (transferFunctionType == TransferFunctionType.STEP) {
//			this.setLearningRule(new BinaryDeltaRule(this));
//		} else if (transferFunctionType == TransferFunctionType.SIGMOID) {
//			this.setLearningRule(new SigmoidDeltaRule(this));
//		} else if (transferFunctionType == TransferFunctionType.TANH) {
//			this.setLearningRule(new SigmoidDeltaRule(this));
//		} else {
//			this.setLearningRule(new PerceptronLearning(this));
//		}

//    //neuralnet = new Perceptron( 3*TrainingSetGenerator.DIMENSION*TrainingSetGenerator.DIMENSION, 2*TrainingSetGenerator.DIMENSION*TrainingSetGenerator.DIMENSION );
//    neuralnet = new NeuralNetwork();
//    Layer layero = new Layer();
//    neuralnet.addLayer( layero );
//
//    Neuron[] ineurons = new InputNeuron[3 * TrainingSetGenerator.DIMENSION * TrainingSetGenerator.DIMENSION];
//    for ( i = 0; i < 3 * TrainingSetGenerator.DIMENSION * TrainingSetGenerator.DIMENSION; i++ )
//    {
//      ineurons[i] = new InputNeuron();
//    }
//    neuralnet.setInputNeurons( Arrays.asList( ineurons ) );
//
//    Connection c;
//    Neuron[] oneurons = new Neuron[2 * TrainingSetGenerator.DIMENSION * TrainingSetGenerator.DIMENSION];
//    double w;
//    for ( i = 0; i < 2 * TrainingSetGenerator.DIMENSION * TrainingSetGenerator.DIMENSION; i++ )
//    {
//      oneurons[i] = new Neuron();
//      oneurons[i].setTransferFunction( new Linear() );
//      layero.addNeuron( oneurons[i] );
//      for ( j = 0; j < 3 * TrainingSetGenerator.DIMENSION * TrainingSetGenerator.DIMENSION; j++ )
//      {
//        c = new Connection( ineurons[j], oneurons[i] );
//        if ( (j / 3 == i / 2) && (j % 3 != 2) )
//        {
//          w = 1.0;
//        }
//        else
//        {
//          w = 0.0;
//        }
//        c.getWeight().setValue( w );
//        //System.out.println( "Weight " + i + " " + j + "    " + w );
//        oneurons[i].addInputConnection( c );
//      }
//    }
//    neuralnet.setOutputNeurons( Arrays.asList( oneurons ) );
//
//
//    //neuralnet.randomizeWeights( 0.000001, 0.0007 );
//      neuralnet.randomizeWeights( 0.000001, 0.01 );
//
//    IterativeLearning learning = new LMS();
//    neuralnet.setLearningRule( learning );
//    neuralnet.addObserver( this );
//    learning.addObserver( this );
//    learning.setMaxIterations( 10 );

  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainpanel = new javax.swing.JPanel();
    svgpanel = new javax.swing.JPanel();
    foregroundrasterlabel = new javax.swing.JLabel();
    rasterlabel = new javax.swing.JLabel();
    predictlabel = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();
    forefftlabel = new javax.swing.JLabel();
    fftlabel = new javax.swing.JLabel();
    predictfftlabel = new javax.swing.JLabel();
    buttonpanel = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    jLabel5 = new javax.swing.JLabel();
    noimagesfield = new javax.swing.JTextField();
    createtrainingsertbutton = new javax.swing.JButton();
    nextimagebutton = new javax.swing.JButton();
    stddevlabel = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jLabel4 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    learningratefield = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    learningstepsfield = new javax.swing.JTextField();
    learnbutton = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setName("Form"); // NOI18N

    mainpanel.setName("mainpanel"); // NOI18N
    mainpanel.setLayout(new java.awt.GridLayout(2, 4));

    svgpanel.setName("svgpanel"); // NOI18N
    svgpanel.setLayout(new java.awt.BorderLayout());
    mainpanel.add(svgpanel);

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.qyouti.QyoutiApp.class).getContext().getResourceMap(TrainingFrame.class);
    foregroundrasterlabel.setBackground(resourceMap.getColor("foregroundrasterlabel.background")); // NOI18N
    foregroundrasterlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    foregroundrasterlabel.setText(resourceMap.getString("foregroundrasterlabel.text")); // NOI18N
    foregroundrasterlabel.setName("foregroundrasterlabel"); // NOI18N
    foregroundrasterlabel.setOpaque(true);
    mainpanel.add(foregroundrasterlabel);

    rasterlabel.setBackground(resourceMap.getColor("rasterlabel.background")); // NOI18N
    rasterlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    rasterlabel.setText(resourceMap.getString("rasterlabel.text")); // NOI18N
    rasterlabel.setName("rasterlabel"); // NOI18N
    rasterlabel.setOpaque(true);
    mainpanel.add(rasterlabel);

    predictlabel.setBackground(resourceMap.getColor("predictlabel.background")); // NOI18N
    predictlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    predictlabel.setName("predictlabel"); // NOI18N
    predictlabel.setOpaque(true);
    mainpanel.add(predictlabel);

    jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
    jLabel1.setName("jLabel1"); // NOI18N
    mainpanel.add(jLabel1);

    forefftlabel.setBackground(resourceMap.getColor("forefftlabel.background")); // NOI18N
    forefftlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    forefftlabel.setName("forefftlabel"); // NOI18N
    forefftlabel.setOpaque(true);
    mainpanel.add(forefftlabel);

    fftlabel.setBackground(resourceMap.getColor("fftlabel.background")); // NOI18N
    fftlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    fftlabel.setName("fftlabel"); // NOI18N
    fftlabel.setOpaque(true);
    mainpanel.add(fftlabel);

    predictfftlabel.setBackground(resourceMap.getColor("predictfftlabel.background")); // NOI18N
    predictfftlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    predictfftlabel.setName("predictfftlabel"); // NOI18N
    predictfftlabel.setOpaque(true);
    mainpanel.add(predictfftlabel);

    getContentPane().add(mainpanel, java.awt.BorderLayout.CENTER);

    buttonpanel.setName("buttonpanel"); // NOI18N
    buttonpanel.setLayout(new java.awt.GridLayout(3, 1));

    jPanel1.setName("jPanel1"); // NOI18N

    jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
    jLabel5.setName("jLabel5"); // NOI18N
    jPanel1.add(jLabel5);

    noimagesfield.setColumns(5);
    noimagesfield.setText(resourceMap.getString("noimagesfield.text")); // NOI18N
    noimagesfield.setName("noimagesfield"); // NOI18N
    jPanel1.add(noimagesfield);

    createtrainingsertbutton.setText(resourceMap.getString("createtrainingsertbutton.text")); // NOI18N
    createtrainingsertbutton.setName("createtrainingsertbutton"); // NOI18N
    createtrainingsertbutton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createtrainingsertbuttonActionPerformed(evt);
      }
    });
    jPanel1.add(createtrainingsertbutton);

    nextimagebutton.setText(resourceMap.getString("nextimagebutton.text")); // NOI18N
    nextimagebutton.setName("nextimagebutton"); // NOI18N
    nextimagebutton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nextimagehandler(evt);
      }
    });
    jPanel1.add(nextimagebutton);

    stddevlabel.setText(resourceMap.getString("stddevlabel.text")); // NOI18N
    stddevlabel.setName("stddevlabel"); // NOI18N
    jPanel1.add(stddevlabel);

    buttonpanel.add(jPanel1);

    jPanel2.setName("jPanel2"); // NOI18N

    jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
    jLabel4.setName("jLabel4"); // NOI18N
    jPanel2.add(jLabel4);

    jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
    jLabel2.setName("jLabel2"); // NOI18N
    jPanel2.add(jLabel2);

    learningratefield.setColumns(10);
    learningratefield.setText(resourceMap.getString("learningratefield.text")); // NOI18N
    learningratefield.setName("learningratefield"); // NOI18N
    jPanel2.add(learningratefield);

    jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
    jLabel3.setName("jLabel3"); // NOI18N
    jPanel2.add(jLabel3);

    learningstepsfield.setColumns(5);
    learningstepsfield.setText(resourceMap.getString("learningstepsfield.text")); // NOI18N
    learningstepsfield.setName("learningstepsfield"); // NOI18N
    jPanel2.add(learningstepsfield);

    learnbutton.setText(resourceMap.getString("learnbutton.text")); // NOI18N
    learnbutton.setName("learnbutton"); // NOI18N
    learnbutton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        learnbuttonActionPerformed(evt);
      }
    });
    jPanel2.add(learnbutton);

    buttonpanel.add(jPanel2);

    jPanel3.setName("jPanel3"); // NOI18N
    buttonpanel.add(jPanel3);

    getContentPane().add(buttonpanel, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void updatedisplay()
  {
    previewcanvas.setSVGDocument( gen.getCurrentSVG() );
    foregroundrasterlabel.setIcon( new ImageIcon( QyoutiUtils.resizeImage( gen.getCurrentForegroundBufferedImage(), imagedisplaywidth, imagedisplaywidth ) ) );
    rasterlabel.setIcon( new ImageIcon( QyoutiUtils.resizeImage( gen.getCurrentBufferedImage(), imagedisplaywidth, imagedisplaywidth ) ) );
    forefftlabel.setIcon( new ImageIcon( QyoutiUtils.resizeImage( gen.getCurrentForegroundFFTImage(), imagedisplaywidth, imagedisplaywidth ) ) );
    fftlabel.setIcon( new ImageIcon( QyoutiUtils.resizeImage( gen.getCurrentFFTImage(), imagedisplaywidth, imagedisplaywidth ) ) );


    double[] input = gen.getCurrentNeuralNetInput();
    for ( int i = 0; i < 4; i++ )
    {
      System.out.println( input[i] );
    }
    neuralnet.setInput( input );
    System.out.println( "Calculating neural net." );
    neuralnet.calculate();
    System.out.println( "Done." );

    updatennetoutputdisplay();
  }

  private void updatennetoutputdisplay()
  {
    int i;
    double[] output = neuralnet.getOutput();
    double[] input = gen.getCurrentNeuralNetInput();
    double[] target = gen.getCurrentNeuralNetTarget();

    float[] dirtyfft = gen.getCurrentFFT();
    float[] predictedfft = new float[dirtyfft.length];
    double scaling;

    for ( i=0; i< output.length; i++ )
    {
      scaling = output[i] / input [i];
//      System.out.println( "output " + i + "  = " + output[i] );
      predictedfft[i*2] = dirtyfft[i*2] * (float)scaling;
      predictedfft[i*2 + 1] = dirtyfft[i*2 + 1] * (float)scaling;
    }
//    System.out.println( "Target, Output "  +
//              gen.getCurrentForegroundFFT()[i] + ", " + gen.getCurrentForegroundFFT()[i+1] + "      " +
//              fftout[i] + ", " + fftout[i+1] );
    BufferedImage predictfft = FastFourierTransform2D.toBufferedImage( predictedfft );
    FastFourierTransform2D.fft2d( predictedfft, 2, -1 );
//    for ( i=0; i< output.length; i++ )
//    {
//      System.out.println( "in/target/out/img " + i + "   " +input[i] + "   " + target[i] + "  " + output[i] + "  "+ predictedfft[i] );
//    }

    for ( i=0; i< output.length; i++ )
    {
      if ( Double.isNaN( output[i] ) || Double.isInfinite( output[i] ))
      {
        System.out.println( "LEARNING GONE WRONG!!!!!" );
        break;
      }
    }
    BufferedImage predict = FastFourierTransform2D.toBufferedImage( predictedfft );
    ImageStatistics stats = new ImageStatistics( predict );
    stddevlabel.setText( "Std Dev = " + stats.getStandardDeviation() );

    predictlabel.setIcon( new ImageIcon( QyoutiUtils.resizeImage( predict, imagedisplaywidth, imagedisplaywidth ) ) );
    predictfftlabel.setIcon( new ImageIcon( QyoutiUtils.resizeImage( predictfft, imagedisplaywidth, imagedisplaywidth ) ) );
  }

  private void nextimagehandler(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextimagehandler
  {//GEN-HEADEREND:event_nextimagehandler
    gen.createImage( false );
    updatedisplay();
  }//GEN-LAST:event_nextimagehandler

  private void createtrainingsertbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createtrainingsertbuttonActionPerformed
  {//GEN-HEADEREND:event_createtrainingsertbuttonActionPerformed
    SetupTask task = new SetupTask( Integer.parseInt( noimagesfield.getText() ) );
    Thread thread = new Thread( task );
    thread.start();
  }

  @Override
  public void update( Observable o, Object arg )
  {
    if ( o == neuralnet )
    {
      System.out.println( "Neural net says it has changed." );
      updatennetoutputdisplay();
    }
    else
    {
      System.out.println( "Other object says it has changed." );
    }
  }//GEN-LAST:event_createtrainingsertbuttonActionPerformed

  private void learnbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_learnbuttonActionPerformed
  {//GEN-HEADEREND:event_learnbuttonActionPerformed
    learning.setLearningRate( Double.parseDouble( learningratefield.getText() ) );
    learning.setMaxIterations( Integer.parseInt( learningstepsfield.getText() ) );
    neuralnet.learnInNewThread( gen.getTrainingSet() );
  }//GEN-LAST:event_learnbuttonActionPerformed

  class SetupTask
          implements Runnable
  {

    int n;

    SetupTask( int count )
    {
      n = count;
      gen = new TrainingSetGenerator();
    }

    @Override
    public void run()
    {
      for ( int i = 0; i < n; i++ )
      {
        gen.createImage( true );
        updatedisplay();
        try
        {
          Thread.sleep( 100 );
        }
        catch ( InterruptedException ex )
        {
        }
      }
    }
  }

  public static void main( String[] args )
  {
    TrainingFrame frame = new TrainingFrame();
    frame.setSize( 900, 400 );
    frame.setVisible( true );

//    TrainingVectorImage image = new TrainingVectorImage( gen, TrainingVectorImage.MARKED );
//    QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/box.svg", image.getSVGDocument().getDocumentElement(), true );
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonpanel;
  private javax.swing.JButton createtrainingsertbutton;
  private javax.swing.JLabel fftlabel;
  private javax.swing.JLabel forefftlabel;
  private javax.swing.JLabel foregroundrasterlabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JButton learnbutton;
  private javax.swing.JTextField learningratefield;
  private javax.swing.JTextField learningstepsfield;
  private javax.swing.JPanel mainpanel;
  private javax.swing.JButton nextimagebutton;
  private javax.swing.JTextField noimagesfield;
  private javax.swing.JLabel predictfftlabel;
  private javax.swing.JLabel predictlabel;
  private javax.swing.JLabel rasterlabel;
  private javax.swing.JLabel stddevlabel;
  private javax.swing.JPanel svgpanel;
  // End of variables declaration//GEN-END:variables
}
