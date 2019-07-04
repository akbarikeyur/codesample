//
//  SignUp.swift
//
//
//  Created by Keyur on 21/12/17.
//  Copyright © 2017 Keyur. All rights reserved.
//


import UIKit
import FRHyperLabel

class SignUp: UIViewController, UITextFieldDelegate {
    
    @IBOutlet weak var emailTxt: TextField!
    @IBOutlet weak var preferNameTxt: TextField!
    @IBOutlet weak var pwdTxt: TextField!
    @IBOutlet var lbl: FRHyperLabel!
    
    @IBOutlet weak var referralTxt: TextField!
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
        
        let string              = "By clicking the button below, I agree to be bound by ToShare's User Agreement and Privacy Policy. I consent to receving these policies online."
        lbl.attributedText = NSAttributedString(string: string)

        let arr:Array = ["User Agreement", "Privacy Policy"]
        let handler = {
            (hyperLabel: FRHyperLabel?, substring: String?) -> Void in
            if(substring == arr[0]){
                let vc = STORYBOARD.LOGIN.instantiateViewController(withIdentifier: "UserAgreementVC") as! UserAgreementVC
                self.navigationController?.pushViewController(vc, animated: true)
            }
            else{
                let vc = STORYBOARD.LOGIN.instantiateViewController(withIdentifier: "PrivacyPolicyVC") as! PrivacyPolicyVC
                self.navigationController?.pushViewController(vc, animated: true)
            }
        }
        lbl.setLinksForSubstrings(arr, withLinkHandler: handler)
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
        let loginViewHt:CGFloat = 280
        
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
    @IBAction func onBackBtnTap(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    @IBAction func onRegisterBtnTap(_ sender: Any?) {
        self.view.endEditing(true)
        if emailTxt.text?.trimmed.count == 0
        {
            displayToast(NSLocalizedString("enter_email", comment: ""))
        }
        else if emailTxt.text?.trimmed.isValidEmail == false
        {
            displayToast(NSLocalizedString("invalid_email", comment: ""))
        }
        else  if preferNameTxt.text?.trimmed.count == 0
        {
            displayToast(NSLocalizedString("enter_prefer_name", comment: ""))
        }
        else  if pwdTxt.text?.count == 0
        {
            displayToast(NSLocalizedString("enter_password", comment: ""))
        }
        else if (pwdTxt.text?.count)! < CONSTANT.MIN_PWD_CHAR
        {
            displayToast(String(format: NSLocalizedString("password_min_char", comment: ""), String(CONSTANT.MIN_PWD_CHAR)))
        }
        else
        {
            showAlertWithOption("", message: "Agreed with User Agreement & Privacy Policy?", completionConfirm: {
                let location = AppModel.shared.currentUser.currLocation
                AppModel.shared.currentUser = UserModel()
                AppModel.shared.currentUser.currLocation = location
                AppModel.shared.currentUser.Email = self.emailTxt.text!.trimmed
                AppModel.shared.currentUser.Password = self.pwdTxt.text!
                AppModel.shared.currentUser.PreferredName = self.preferNameTxt.text!.encoded
                AppModel.shared.currentUser.Referral = self.referralTxt.text!.encoded
                
                LoginSignupService.shared.serviceCallToRegister {
                    AppModel.shared.currentUser.Id = nil
                    showAlert("", message: NSLocalizedString("register_success", comment: ""),completion: {
                        self.navigationController?.popViewController(animated: true)
                    })
                }
            }, completionCancel: {
                
            })
        }
    }
    
    //MARK:- Textfield delegate
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        var maxLength:Int = 0
        var str:String = ""
        if(textField == emailTxt){
            maxLength = CONSTANT.MAX_EMAIL_CHAR
            str = emailTxt.text!
        }
        else if(textField == preferNameTxt){
            maxLength = CONSTANT.MAX_PREFER_NAME_CHAR
            str = preferNameTxt.text!
        }
        else{
            return true
        }
        return str.count < maxLength
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool
    {
        if textField == emailTxt
        {
            preferNameTxt.becomeFirstResponder()
        }
        else if textField == preferNameTxt
        {
            pwdTxt.becomeFirstResponder()
        }
        else if textField == pwdTxt
        {
            referralTxt.becomeFirstResponder()
        }
        else if textField == referralTxt
        {
            onRegisterBtnTap(self)
        }
        return true
    }
    
   
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
}
