//
//  LoginVC.swift
//
//
//  Created by Keyur on 21/12/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit
import IQKeyboardManagerSwift

class LoginVC: UIViewController, UITextFieldDelegate {

    @IBOutlet weak var emailTxt: TextField!
    @IBOutlet weak var pwdTxt: TextField!
    @IBOutlet weak var contentView: View!
    
    @IBOutlet weak var logoWdConstraint: NSLayoutConstraint!
    @IBOutlet weak var logoHtConstraint: NSLayoutConstraint!
    
    @IBOutlet weak var headerBottomConstraint: NSLayoutConstraint!
    @IBOutlet weak var logoBottomConstraint: NSLayoutConstraint!
    @IBOutlet weak var loginViewBottomContraint: NSLayoutConstraint!
    
    var keyboardHeight:CGFloat = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: NSNotification.Name.UIKeyboardWillShow, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: NSNotification.Name.UIKeyboardWillHide, object: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let tabBar : CustomTabBarController = self.tabBarController as! CustomTabBarController
        tabBar.setTabBarHidden(tabBarHidden: true)
        
    }
    override func viewWillDisappear(_ animated: Bool) {
        self.view.endEditing(true)
    }

    
    @objc func keyboardWillShow(notification: NSNotification) {
        let userInfo:NSDictionary = notification.userInfo! as NSDictionary
        let keyboardFrame:NSValue = userInfo.value(forKey: UIKeyboardFrameEndUserInfoKey) as! NSValue
        let keyboardRectangle = keyboardFrame.cgRectValue
        keyboardHeight = keyboardRectangle.height
        autoLayout()
    }
    
    @objc func keyboardWillHide(notification: NSNotification) {
        keyboardHeight = 0
        autoLayout()
    }
    
    //MARK:- auto layout
    override func viewWillLayoutSubviews() {
        self.autoLayout()
    }
    
    func autoLayout(){
        
        let screenHt:CGFloat = contentView.frame.size.height - keyboardHeight
        let headerHt:CGFloat = 44
        let logoWdHt:CGFloat = 155
        var logoMinWdHt:CGFloat = logoWdHt
        let loginViewHt:CGFloat = 195
        var padding:CGFloat = (screenHt - (headerHt+logoWdHt+loginViewHt))/3
        if(padding < 10){
            padding = 10
            logoMinWdHt = (screenHt - (headerHt+loginViewHt) - (3 * padding))
        }
        
        logoWdConstraint.constant = logoMinWdHt
        logoHtConstraint.constant = logoMinWdHt

        headerBottomConstraint.constant = padding
        logoBottomConstraint.constant = padding
        loginViewBottomContraint.constant = keyboardHeight == 0 ? padding : keyboardHeight
        
        self.view.layoutIfNeeded()
    }
    //MARK:- Button Tap
    @IBAction func onRegisterBtnTap(_ sender: Any) {
        let vc = STORYBOARD.LOGIN.instantiateViewController(withIdentifier: "SignUp") as! SignUp
        self.navigationController?.pushViewController(vc, animated: true)
    }
    @IBAction func onBackBtnTap(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    @IBAction func onLoginBtnTap(_ sender: Any)
    {
        self.view.endEditing(true)
        if emailTxt.text?.trimmed.count == 0
        {
            displayToast(NSLocalizedString("enter_email", comment: ""))
        }
        else if emailTxt.text?.trimmed.isValidEmail == false
        {
            displayToast(NSLocalizedString("invalid_email", comment: ""))
        }
        else  if pwdTxt.text?.count == 0
        {
            displayToast(NSLocalizedString("enter_password", comment: ""))
        }
        else
        {
            let location = AppModel.shared.currentUser.currLocation
            AppModel.shared.currentUser = UserModel()
            AppModel.shared.currentUser.currLocation = location
            AppModel.shared.currentUser.Email = self.emailTxt.text!
            AppModel.shared.currentUser.Password = self.pwdTxt.text!
            
            
            LoginSignupService.shared.serviceCallToLogin {
                LoginSignupService.shared.serviceCallToGetCurrentUser{
                }
                IQKeyboardManager.sharedManager().enable = true
                self.navigationController?.popToRootViewController(animated: true)
            }
        }
    }
    
    @IBAction func onForgotPwdBtnTap(_ sender: Any)
    {
        let vc = STORYBOARD.LOGIN.instantiateViewController(withIdentifier: "ForgotPwdVC") as! ForgotPwdVC
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    //MARK:- textfield delegate
    func textFieldShouldReturn(_ textField: UITextField) -> Bool
    {
        if textField == emailTxt
        {
            pwdTxt.becomeFirstResponder()
        }
        else if textField == pwdTxt
        {
            onLoginBtnTap(self)
        }
        return true
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}

