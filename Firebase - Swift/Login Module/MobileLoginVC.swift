//
//  MobileLoginVC.swift
//  Check-Up
//
//  Created by Keyur on 11/16/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit
import Firebase
import FirebaseAuth
import FirebaseDatabase

class MobileLoginVC: UIViewController {

    @IBOutlet weak var mobileTxt: UITextField!
    @IBOutlet var verificationContainerView: UIView!
    @IBOutlet weak var verificationCodeTxt: UITextField!
    
    @IBOutlet weak var sendBtn: UIButton!
    var isLoging:Bool = true
    var _verificationId:String = ""
    var _phoneNo:String = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if(isLoging){
            sendBtn.setTitle("LOGIN", for: .normal)
        }
        // Do any additional setup after loading the view.
    }

    @IBAction func cleckToSendCode(_ sender: Any)
    {
        
        _phoneNo = mobileTxt.text!
        displayLoader()
        PhoneAuthProvider.provider().verifyPhoneNumber(_phoneNo, uiDelegate: nil) { (verificationID, error) in
            removeLoader()
            if let error = error{
                displayToast( error.localizedDescription)
                return
            }
            if verificationID == nil{
                displayToast("Something is wrong.")
                return
            }
            self._verificationId = verificationID!
            displaySubViewtoParentView(self.view, subview: self.verificationContainerView)
        }
    }
    @IBAction func clickToSubmitCode(_ sender: Any)
    {
        verificationContainerView.removeFromSuperview()
        AppModel.shared.currentUser = UserModel.init(uID:  "", email: "", password: "", phoneNo:_phoneNo, phoneCode:verificationCodeTxt.text!, phoneId:_verificationId, username: "", name: "", location: LocationModel.init(id: "", name: "", image: "", address: "", latitude: 0.0, longitude: 0.0, isOpen: true), height: "", age: 0, position: USER.POSITION, distance: USER.DISTANCE, local_pic_url: "", remote_pic_url: "", login_type: USER.MOBILE_LOGIN, user_type: USER.REGULAR_USER, courts : [String](), last_seen: "", story: [String] (), contact: [ContactModel](), fcmToken:AppDelegate().sharedDelegate().getFcmToken(), badge : 0, curr_court:"",total_checkIn:0, blockUsers:[String]())
       
        AppDelegate().sharedDelegate().signUpWithMobile()
    }
    
    @IBAction func clickToCancel(_ sender: Any)
    {
        verificationContainerView.removeFromSuperview()
    }
    
    @IBAction func clickToBack(_ sender: Any)
    {
        self.navigationController?.popViewController(animated: true)
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
