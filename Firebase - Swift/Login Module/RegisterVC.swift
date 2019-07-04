//
//  RegisterVC.swift
//  Check-Up
//
//  Created by Keyur on 10/08/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit

class RegisterVC: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    @IBAction func clickToBack(_ sender: Any)
    {
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func clickToEmailSignup(_ sender: Any)
    {
        let vc : CreateProfileVC = self.storyboard?.instantiateViewController(withIdentifier: "CreateProfileVC") as! CreateProfileVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    @IBAction func clickToFacebook(_ sender: Any)
    {
        AppDelegate().sharedDelegate().loginWithFacebook()
    }
    
    @IBAction func clickToPhoneNumber(_ sender: Any) {
        let vc : MobileLoginVC = self.storyboard?.instantiateViewController(withIdentifier: "MobileLoginVC") as! MobileLoginVC
        vc.isLoging = false
        self.navigationController?.pushViewController(vc, animated: true)
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
