//
//  SegmentedControl.swift
//  
//
//  Created by Keyur on 21/05/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import UIKit

//@IBDesignable
class SegmentedControl: UISegmentedControl {

    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */

    @IBInspectable var tintColorType : Int32 = 0 {
        didSet {
            setTintColor(tintColorType: ColorType(rawValue: self.tintColorType))
        }
    }
    
}
