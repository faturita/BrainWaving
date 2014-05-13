package notegenerator;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.ExponentialRangeModel;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.FunctionOscillator;
import com.jsyn.unitgen.ImpulseOscillator;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SawtoothOscillatorDPW;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitSource;
import com.jsyn.util.WaveFileWriter;
import com.jsyn.util.WaveRecorder;

/**
 * Play a sawtooth using a JSyn oscillator and some knobs.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * 
 */
public class SawFaders extends JApplet
{
	private static final long serialVersionUID = -2704222221111608377L;
	private Synthesizer synth;
	private UnitOscillator osc;
	private LinearRamp lag;
	private LineOut lineOut;

	private UnitSource unitSource;
	
	AudioScope scope;
	
	public void initSynthesizer()
	{
		synth = JSyn.createSynthesizer();
		
		// Add a tone generator. (band limited sawtooth)
		osc = new SawtoothOscillatorBL(); 
		
		//osc = new SawtoothOscillatorDPW();
		
		//osc = new SineOscillator();
		
		//osc = new SquareOscillator();
		
		
		synth.add( osc );
		
		// Add a lag to smooth out amplitude changes and avoid pops.
		synth.add( lag = new LinearRamp() );
		// Add an output mixer.
		synth.add( lineOut = new LineOut() );
		// Connect the oscillator to both left and right output.
		//osc.output.connect( 0, lineOut.input, 0 );
		osc.output.connect( 0, lineOut.input, 1 );
		
		
		unitSource = new WindCircuit();
		synth.add( unitSource.getUnitGenerator() );
		
		// Connect the source to both left and right speakers.
		unitSource.getOutput().connect( 0, lineOut.input, 0 );
		
		// Set the minimum, current and maximum values for the port.
		lag.output.connect( osc.amplitude );
		lag.input.setup( 0.0, 0.5, 1.0 );
		lag.time.set(  0.2 );
		
		osc.frequency.setup( 50.0, 300.0, 10000.0 );
		
		
	}
	
	public void changeFrequency(double frequency)
	{
		osc.frequency.set(frequency);
	}
	
	public void init()
	{
		initSynthesizer();
		
		//add( PortControllerFactory.createExponentialPortSlider( osc.frequency ) );

		final JButton button;
		add( button = new JButton());
		
		button.setText("Record");
		
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (button.getText().equals("Record"))
					button.setText("Stop");
				else
					button.setText("Record");
				
			}
			
		});
		
		// Arrange the faders in a stack.
		setLayout( new GridLayout( 0, 1 ) );

		ExponentialRangeModel amplitudeModel = PortModelFactory.createExponentialModel( lag.input );
		RotaryTextController knob = new RotaryTextController( amplitudeModel, 5 );
		JPanel knobPanel = new JPanel();
		knobPanel.add( knob );
		add( knobPanel );
		
		// Use a scope to see the output.
		scope = new AudioScope( synth );
		scope.addProbe( osc.getOutput() );
		scope.setTriggerMode( AudioScope.TriggerMode.NORMAL );
		scope.getView().setControlsVisible( false );
		//add( BorderLayout.SOUTH, scope.getView() );
		
		add(scope.getView());

		
		validate();
	}

	public void start()
	{
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		
		if (scope != null)
			scope.start();
		
		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		lineOut.start();
	}

	public void stop()
	{
		synth.stop();
		if (scope!=null)
			scope.stop();
	}

	public static void initSawFadersApplet(SawFaders applet, String title)
	{
		JAppletFrame frame = new JAppletFrame( title, applet );
		frame.setSize( 440, 600 );
		frame.setVisible( true );
		frame.test();
		

	}
	
	/* Can be run as either an application or as an applet. */
	public static void main( String args[] )
	{
		SawFaders applet = new SawFaders();
		initSawFadersApplet(applet,"Brain Sounds");
	}
	
	private WaveRecorder recorder;
	
	public void initRecording( String filename ) throws FileNotFoundException
	{
		File waveFile = new File( filename );
		// Default is stereo, 16 bits.
		recorder = new WaveRecorder( synth, waveFile );
		
		osc.output.connect( 0, recorder.getInput(), 0 ); // left
		osc.output.connect( 0, recorder.getInput(), 1 ); // right
		
		recorder.start();
	}
	
	public void finishRecording() throws IOException 
	{
		recorder.stop();
		recorder.close();
	}

}
