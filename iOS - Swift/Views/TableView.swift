//
//  TableView.swift
//  
//
//  Created by Keyur on 2/18/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import UIKit

//@IBDesignable
class TableView: UITableView {

    override init(frame: CGRect, style: UITableViewStyle) {
        super.init(frame: frame, style: style)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.tableFooterView = UIView()
        self.rowHeight = UITableViewAutomaticDimension
        self.estimatedSectionHeaderHeight = UITableViewAutomaticDimension
    }
    
    /// background color  types
    @IBInspectable var backgroundColorTypeAdapter : Int32 = 0 {
        didSet {
            self.backgroundColorType = ColorType(rawValue: self.backgroundColorTypeAdapter)
        }
    }
    
    /// background color  types
    var backgroundColorType : ColorType? {
        didSet {
            setBackgroundColor(backgroundColorType: backgroundColorType)
        }
    }
}


