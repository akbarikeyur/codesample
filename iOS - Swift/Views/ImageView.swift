//
//  ImageView.swift
//  
//
//  Created by Keyur on 2/18/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import UIKit

//@IBDesignable
class ImageView: UIImageView {
    
    var isDownloadingImage = false
    
    @IBInspectable var backgroundColorTypeAdapter : Int32 = 0 {
        didSet {
            self.backgroundColorType = ColorType(rawValue: self.backgroundColorTypeAdapter)
        }
    }
    var backgroundColorType : ColorType? {
        didSet {
            setBackgroundColor(backgroundColorType: backgroundColorType)
        }
        
    }
    
    @IBInspectable var cornerRadius : CGFloat = 0 {
        didSet {
            setCornerRadius(cornerRadius)
        }
    }
 
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        initialzeImageView()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        initialzeImageView()
    }
    
    override init(image: UIImage?) {
        super.init(image: image)
        
        initialzeImageView()
    }
    
    override init(image: UIImage?, highlightedImage: UIImage?) {
        super.init(image: image, highlightedImage: highlightedImage)
        
        initialzeImageView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    func initialzeImageView() {
//        kf_showIndicatorWhenLoading = true
    }

    func downloadImage(from url : String?, placeHolderImageName : String?) {
//        isDownloadingImage = true
//
//        let placeHolder = placeHolderImageName != nil ? UIImage(named: placeHolderImageName!) : nil
//
//        if isEmptyString(string: url) == false {
//            self.kf_setImageWithURL(NSURL(string: url!)!, placeholderImage: placeHolder, optionsInfo: nil, progressBlock: nil, completionHandler: { (image, error, cacheType, imageURL) in
//                self.isDownloadingImage = false
//                if image == nil {
//                    self.image = placeHolder
//                }
//            })
//        } else {
//            isDownloadingImage = false
//            self.image = placeHolder
//        }
    }
}
