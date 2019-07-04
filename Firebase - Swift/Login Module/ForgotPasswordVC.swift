//
//  ForgotPasswordVC.swift
//  Check-Up
//
//  Created by Keyur on 05/10/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit

class ForgotPasswordVC: UIViewController {

    @IBOutlet weak var emailTxt: UITextField!
    @IBOutlet weak var sendBtn: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    @IBAction func clickToBack(_ sender: Any)
    {
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func clickToSend(_ sender: Any)
    {
        if emailTxt.text?.count == 0
        {
            displayToast("Please enter email.")
        }
        else if emailTxt.text?.isValidEmail == false
        {
            displayToast("Invalid email.")
        }
        else
        {
            AppDelegate().sharedDelegate().forgotPassword(email: emailTxt.text!)
            clickToBack(self)
        }
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
