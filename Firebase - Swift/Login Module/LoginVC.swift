//
//  LoginVC.swift
//  Check-Up
//
//  Created by Keyur on 10/08/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit


class LoginVC: UIViewController, UITextFieldDelegate {

    @IBOutlet var emailTxt: UITextField!
    @IBOutlet var passwordTxt: UITextField!
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    @IBAction func clickToBack(_ sender: Any)
    {
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func clickToLoginWithFacebook(_ sender: Any)
    {
        self.view.endEditing(true)
        AppDelegate().sharedDelegate().loginWithFacebook()
    }
    @IBAction func clickToPhoneNumber(_ sender: Any) {
        let vc : MobileLoginVC = self.storyboard?.instantiateViewController(withIdentifier: "MobileLoginVC") as! MobileLoginVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    @IBAction func clickToForgotPassword(_ sender: Any)
    {
        self.view.endEditing(true)
        let vc : ForgotPasswordVC = self.storyboard?.instantiateViewController(withIdentifier: "ForgotPasswordVC") as! ForgotPasswordVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    func clickToLogin()
    {
        if emailTxt.text?.count == 0
        {
            displayToast("Please enter email.")
        }
        else if emailTxt.text?.isValidEmail == false
        {
            displayToast( "Invalid email.")
        }
        else if passwordTxt.text?.count == 0
        {
            displayToast("Please enter password.")
        }
        else
        {
            
            AppModel.shared.currentUser = UserModel.init(uID: "", email: emailTxt.text!, password: passwordTxt.text!, phoneNo:"", phoneCode:"", phoneId:"",username: "", name: "", location: LocationModel.init(id: "", name: "", image: "", address: "", latitude: 0.0, longitude: 0.0, isOpen: true), height: "", age: USER.AGE, position: USER.POSITION, distance: USER.DISTANCE, local_pic_url: "", remote_pic_url: "", login_type: USER.EMAIL_LOGIN, user_type: USER.REGULAR_USER, courts : [String](), last_seen: "", story: [String](), contact : [ContactModel](), fcmToken:AppDelegate().sharedDelegate().getFcmToken(), badge : 0, curr_court:"", total_checkIn : 0, blockUsers:[String]())
        
            
            AppDelegate().sharedDelegate().loginWithEmail()
        }
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool
    {
        if textField == emailTxt
        {
            passwordTxt.becomeFirstResponder()
        }
        else if textField == passwordTxt
        {
            passwordTxt.resignFirstResponder()
            clickToLogin()
        }
        return true
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
