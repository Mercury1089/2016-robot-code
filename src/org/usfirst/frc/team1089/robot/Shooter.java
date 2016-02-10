package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

public class Shooter {
	Solenoid shooter;
	DoubleSolenoid elevator;
	public Shooter(){
		shooter = new Solenoid(0);
		elevator = new DoubleSolenoid(2, 3);
	}
	public void raise(int level){ 
		if(level == 1){
			elevator.set(DoubleSolenoid.Value.kForward);
		}
		
		if(level == -1){
			elevator.set(DoubleSolenoid.Value.kReverse);
		}
	}
	public void shoot(){
		shooter.set(true);
	}
	


}
