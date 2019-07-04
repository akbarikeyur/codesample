//
//  CreateProfileVC.swift
//  Check-Up
//
//  Created by Keyur on 10/08/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit
import PEPhotoCropEditor
import Alamofire
import Firebase
import GooglePlacePicker


class CreateProfileVC: UIViewController, customPopUpDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, PECropViewControllerDelegate, GMSPlacePickerViewControllerDelegate,UITextFieldDelegate {

    @IBOutlet var profilePicBtn: UIButton!
    @IBOutlet var userNameTxt: UITextField!
    @IBOutlet var nameTxt: UITextField!
    @IBOutlet var locationLbl: UILabel!
    @IBOutlet var heightTxt: UITextField!
    @IBOutlet var ageTxt: UITextField!
    @IBOutlet var positionPGBtn: UIButton!
    @IBOutlet var positionSGBtn: UIButton!
    @IBOutlet var positionSFBtn: UIButton!
    @IBOutlet var positionPFBtn: UIButton!
    @IBOutlet var positionCBtn: UIButton!
    @IBOutlet var distanceSlider: UISlider!
    @IBOutlet var distanceLbl: UILabel!
    @IBOutlet var emailTxt: UITextField!
    @IBOutlet var passwordTxt: UITextField!
    
    @IBOutlet weak var termsBtn: UIButton!
    var profileImage : UIImage!
    var CustomPopUp: customPopUp!
    var positionVal : Int!
    var arrLocationData = [AnyObject]()
    var selectedLocation : LocationModel!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        setUIDesigning()
        
        
        
    }

    func setUIDesigning()
    {
        profilePicBtn.addCornerRadius(radius: profilePicBtn.frame.size.width/2)
        clickToSelectPosition(positionPGBtn)
        
        
    }
    
    
    @IBAction func clickToBack(_ sender: Any)
    {
        self.view.endEditing(true)
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func clickToUserProfilePic(_ sender: Any)
    {
        self.view.endEditing(true)
        openCustomPopup()
        
    }
    
    @IBAction func clickToSelectPosition(_ sender: UIButton) {
        
        positionPGBtn.isSelected = false
        positionSGBtn.isSelected = false
        positionSFBtn.isSelected = false
        positionPFBtn.isSelected = false
        positionCBtn.isSelected = false
        switch sender {
        case positionPGBtn:
            positionPGBtn.isSelected = true
            positionVal = 1
            break
        case positionSGBtn:
            positionSGBtn.isSelected = true
            positionVal = 2
            break
        case positionSFBtn:
            positionSFBtn.isSelected = true
            positionVal = 3
            break
        case positionPFBtn:
            positionPFBtn.isSelected = true
            positionVal = 4
            break
        case positionCBtn:
            positionCBtn.isSelected = true
            positionVal = 5
            break
        default:
            break
        }
    }
    
    @IBAction func clickToSelectLocation(_ sender: UIButton) {
        self.view.endEditing(true)
        let config = GMSPlacePickerConfig(viewport: nil)
        let placePicker = GMSPlacePickerViewController(config: config)
        placePicker.delegate = self
        present(placePicker, animated: true, completion: nil)
    }
    
    @IBAction func clickToChangeDistance(_ sender: Any)
    {
        distanceLbl.text = String(format: "%0.0f mi", distanceSlider.value)
    }
    @IBAction func onAcceptTerms(_ sender: Any) {
        termsBtn.isSelected = !termsBtn.isSelected
    }
    
    @IBAction func onTermsBtnTap(_ sender: Any) {
        UIApplication.shared.open(URL(string:"http://check-up.webflow.io/terms-conditions-privacy-policy")!, options: [String:Any] (), completionHandler: nil)
    }
    @IBAction func clickToSubmit(_ sender: Any)
    {
        if profileImage == nil {
            displayToast("Please add user picture.")
        }
        else if userNameTxt.text?.count == 0 {
            displayToast( "Please enter username.")
        }
        else if nameTxt.text?.count == 0 {
            displayToast( "Please enter name.")
        }
//        else if locationLbl.text?.count == 0 {
//            displayToast(view: self.view, message: "Please enter location.")
//        }
//        else if heightTxt.text?.count == 0 {
//            displayToast(view: self.view, message: "Please enter height.")
//        }
//        else if ageTxt.text?.count == 0 {
//            displayToast(view: self.view, message: "Please enter age.")
//        }
        else if emailTxt.text?.count == 0 {
            displayToast( "Please enter email.")
        }
        else if emailTxt.text?.isValidEmail == false {
            displayToast("Invalid email.")
        }
        else if passwordTxt.text?.count == 0 {
            displayToast("Please enter password.")
        }
        else if (termsBtn.isSelected == false){
            displayToast("Please accept terms & conditions.")
        }
        else
        {
            let imageName = getCurrentTimeStampValue()
            storeImageInDocumentDirectory(image: profileImage, imageName: imageName)
            
            if selectedLocation == nil
            {
                selectedLocation = LocationModel.init(id: "", name: "", image: "", address: "", latitude: 0.0, longitude: 0.0, isOpen: true)
            }
            
            let age : Int = ageTxt.text! == "" ? 0 : Int(ageTxt.text!)!
           
            AppModel.shared.currentUser = UserModel.init(uID: "", email: emailTxt.text!, password: passwordTxt.text!,phoneNo:"", phoneCode:"", phoneId:"", username: userNameTxt.text!, name: nameTxt.text!, location: LocationModel.init(id: "", name: "", image: "", address: selectedLocation.address!, latitude: selectedLocation.latitude, longitude: selectedLocation.longitude, isOpen: true), height: heightTxt.text!, age: age, position: positionVal, distance: Int(distanceSlider.value), local_pic_url: imageName, remote_pic_url: "", login_type: USER.EMAIL_LOGIN, user_type: USER.REGULAR_USER, courts : [String](), last_seen: "", story: [String](), contact : [ContactModel](), fcmToken:AppDelegate().sharedDelegate().getFcmToken(), badge : 0, curr_court:"", total_checkIn : 0 , blockUsers:[String]())
            
            
            AppDelegate().sharedDelegate().signUpWithEmail()
        }
    }
    //MARK:- textfield delegate
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool
    {
        if textField == userNameTxt && (string == " ") {
            displayToast( "Space not allowed.")
            return false
        }
        return true
    }
   
    //MARK: - Custom Popup
    func openCustomPopup()
    {
        CustomPopUp = self.storyboard?.instantiateViewController(withIdentifier: "customPopUp") as! customPopUp
        CustomPopUp.delegate = self
        self.view.addSubview(CustomPopUp.view)
        let popupSize: CGFloat = CustomPopUp.popupView.frame.size.width
        CustomPopUp.removeView.isHidden = true
        CustomPopUp.constraintWidthCameraView.constant = popupSize / 2
        CustomPopUp.constraintWidthGalleryView.constant = popupSize / 2
        CustomPopUp.constraintWidthRemoveImageView.constant = 0
        
        displaySubViewtoParentView(self.view, subview: CustomPopUp.view)
        
        CustomPopUp.view.transform = CGAffineTransform(scaleX: 0.4, y: 0.4)
        UIView.animate(withDuration: 0.35, delay: 0.0, usingSpringWithDamping: 0.55, initialSpringVelocity: 1.0, options: [], animations: {() -> Void in
            self.CustomPopUp.view.transform = CGAffineTransform.identity
        }, completion: {(_ finished: Bool) -> Void in
        })
    }
    
    func closeToClick()
    {
        UIView.animate(withDuration: 0.25, animations: {() -> Void in
            self.CustomPopUp.view.transform = CGAffineTransform(scaleX: 0.65, y: 0.65)
            self.CustomPopUp.view.alpha = 0.0
        }, completion: {(_ finished: Bool) -> Void in
            self.CustomPopUp.view.removeFromSuperview()
        })
    }
    
    func captureCameraImage()
    {
        if !UIImagePickerController.isSourceTypeAvailable(.camera) {
            displayToast("Your device has no camera")
            CustomPopUp.view.removeFromSuperview()
        }
        else {
            let imgPicker = UIImagePickerController()
            imgPicker.delegate = self
            imgPicker.sourceType = .camera
            self.present(imgPicker, animated: true, completion: {() -> Void in
            })
            CustomPopUp.view.removeFromSuperview()
        }
    }
    
    func selectGalleryImage()
    {
        CustomPopUp.view.removeFromSuperview()
        let imgPicker = UIImagePickerController()
        imgPicker.delegate = self
        imgPicker.sourceType = .photoLibrary
        //imgPicker.mediaTypes = UIImagePickerController.availableMediaTypes(for: .photoLibrary)!
        self.present(imgPicker, animated: true, completion: {() -> Void in
        })
    }
    
    func removeImage()
    {
        
    }
    
    func imagePickerController(_ imgPicker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        // Picking Image from Camera/ Library
        imgPicker.dismiss(animated: true, completion: {() -> Void in
        })
        CustomPopUp.view.removeFromSuperview()
        let selectedImage: UIImage? = (info["UIImagePickerControllerOriginalImage"] as? UIImage)
        if selectedImage == nil {
            return
        }
        let controller = PECropViewController()
        controller.delegate = self as PECropViewControllerDelegate
        controller.image = selectedImage
        controller.keepingCropAspectRatio = true
        controller.toolbarHidden = true
        let width: CGFloat? = selectedImage?.size.width
        let height: CGFloat? = selectedImage?.size.height
        let length: CGFloat = min(width!, height!)
        controller.imageCropRect = CGRect(x: CGFloat((width! - length) / 2), y: CGFloat((height! - length) / 2), width: length, height: length)
        let navigationController = UINavigationController(rootViewController: controller)
        self.present(navigationController, animated: true, completion: { _ in })
    }
    
    func cropViewController(_ controller: PECropViewController, didFinishCroppingImage croppedImage: UIImage) {
        controller.dismiss(animated: true, completion: { _ in })
        // Adjusting Image Orientation
        let imgCompress: UIImage? = compressImage(croppedImage, to: CGSize(width: CGFloat(IMAGESIZE.IMAGE_WIDTH), height: CGFloat(IMAGESIZE.IMAGE_HEIGHT)))
        profilePicBtn.setBackgroundImage(croppedImage, for: .normal)
        profileImage = imgCompress!
    }
    
    func cropViewControllerDidCancel(_ controller: PECropViewController) {
        controller.dismiss(animated: true, completion: { _ in })
    }

    //MARK: - Google Place Picker
    // To receive the results from the place picker 'self' will need to conform to
    // GMSPlacePickerViewControllerDelegate and implement this code.
    func placePicker(_ viewController: GMSPlacePickerViewController, didPick place: GMSPlace) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        
        
        if place.formattedAddress == nil
        {
            displayToast("We can not find your location. Please select again.")
        }
        else
        {
            self.selectedLocation = LocationModel.init(id: "", name: "", image: "", address: place.formattedAddress!, latitude: Float(place.coordinate.latitude), longitude: Float(place.coordinate.longitude), isOpen: true)
            self.locationLbl.text = self.selectedLocation.address
        }
    }
    
    func placePickerDidCancel(_ viewController: GMSPlacePickerViewController) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        
        //print("No place selected")
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
