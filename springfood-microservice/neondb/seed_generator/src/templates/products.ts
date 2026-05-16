/**
 * Vietnamese Food Product Templates
 * 
 * Contains 150+ realistic Vietnamese food product names organized by category.
 * Each product includes name and description in Vietnamese.
 * 
 * Distribution:
 * - Phở: 10 products
 * - Bún: 10 products
 * - Cơm: 12 products
 * - Cà Phê: 8 products
 * - Trà Sữa: 12 products
 * - Nước Ép: 10 products
 * - Sinh Tố: 10 products
 * - Trà Trái Cây: 8 products
 * - Chè: 8 products
 * - Bánh Mì: 8 products
 * - Lẩu: 10 products
 * - Món Nướng: 10 products
 * - Món Ăn Vặt: 10 products
 * - Đồ Ăn Nhanh: 10 products
 * - Nem & Chả: 8 products
 * - Món Hải Sản: 10 products
 * - Xôi: 6 products
 * - Cháo: 5 products
 * 
 * Total: 155 products
 */

export interface ProductTemplate {
  name: string;
  description: string;
  category: string;
}

export const PRODUCT_TEMPLATES: ProductTemplate[] = [
  // Phở (10 products)
  {
    name: 'Phở Bò Tái',
    description: 'Phở bò với thịt bò tái mềm, nước dùng thơm ngon được ninh từ xương trong 12 tiếng',
    category: 'Phở'
  },
  {
    name: 'Phở Bò Chín',
    description: 'Phở bò với thịt bò chín mềm, nước dùng đậm đà hương vị truyền thống',
    category: 'Phở'
  },
  {
    name: 'Phở Bò Viên',
    description: 'Phở với bò viên tươi ngon, dai giòn, nước dùng thanh ngọt',
    category: 'Phở'
  },
  {
    name: 'Phở Gà',
    description: 'Phở gà với thịt gà luộc mềm, nước dùng trong vắt, thơm mùi gừng',
    category: 'Phở'
  },
  {
    name: 'Phở Đặc Biệt',
    description: 'Phở đầy đủ với tái, chín, gầu, gân, sách - món phở truyền thống Hà Nội',
    category: 'Phở'
  },
  {
    name: 'Phở Sốt Vang',
    description: 'Phở bò sốt vang đậm đà, thơm mùi gia vị, ăn kèm bánh mì',
    category: 'Phở'
  },
  {
    name: 'Phở Cuốn',
    description: 'Bánh phở cuốn thịt bò, rau thơm, chấm nước mắm chua ngọt',
    category: 'Phở'
  },
  {
    name: 'Phở Xào',
    description: 'Phở xào thịt bò với rau củ, nước sốt đậm đà',
    category: 'Phở'
  },
  {
    name: 'Phở Gà Quay',
    description: 'Phở với gà quay giòn rụm, nước dùng thơm ngon',
    category: 'Phở'
  },
  {
    name: 'Phở Bò Kho',
    description: 'Phở ăn kèm bò kho tiêu, thơm nồng, đậm đà',
    category: 'Phở'
  },

  // Bún (10 products)
  {
    name: 'Bún Chả',
    description: 'Bún chả Hà Nội với chả nướng thơm, nước mắm chua ngọt đặc trưng',
    category: 'Bún'
  },
  {
    name: 'Bún Bò Huế',
    description: 'Bún bò Huế cay nồng, nước dùng đậm đà với sả, mắm ruốc',
    category: 'Bún'
  },
  {
    name: 'Bún Riêu',
    description: 'Bún riêu cua đồng, nước dùng chua chua, thơm mùi cà chua',
    category: 'Bún'
  },
  {
    name: 'Bún Thịt Nướng',
    description: 'Bún với thịt nướng thơm, rau sống, nước mắm chua ngọt',
    category: 'Bún'
  },
  {
    name: 'Bún Đậu Mắm Tôm',
    description: 'Bún đậu với chả cốm, thịt luộc, chấm mắm tôm đặc biệt',
    category: 'Bún'
  },
  {
    name: 'Bún Mọc',
    description: 'Bún mọc với giò sống, nước dùng trong vắt, thơm ngon',
    category: 'Bún'
  },
  {
    name: 'Bún Ốc',
    description: 'Bún ốc với ốc luộc, cà chua, nước dùng chua cay',
    category: 'Bún'
  },
  {
    name: 'Bún Cá',
    description: 'Bún cá với cá lóc chiên giòn, nước dùng thơm mùi mắm',
    category: 'Bún'
  },
  {
    name: 'Bún Măng Vịt',
    description: 'Bún măng vịt với thịt vịt mềm, măng chua giòn',
    category: 'Bún'
  },
  {
    name: 'Bún Bò Nam Bộ',
    description: 'Bún trộn thịt bò xào, rau thơm, đậu phộng rang',
    category: 'Bún'
  },

  // Cơm (12 products)
  {
    name: 'Cơm Tấm Sườn',
    description: 'Cơm tấm với sườn nướng thơm, chả trứng, bì',
    category: 'Cơm'
  },
  {
    name: 'Cơm Gà Xối Mỡ',
    description: 'Cơm gà Hội An với thịt gà luộc mềm, cơm thơm mùi nghệ',
    category: 'Cơm'
  },
  {
    name: 'Cơm Chiên Dương Châu',
    description: 'Cơm chiên với tôm, xúc xích, trứng, rau củ',
    category: 'Cơm'
  },
  {
    name: 'Cơm Sườn Bì Chả',
    description: 'Cơm tấm đầy đủ với sườn, bì, chả trứng',
    category: 'Cơm'
  },
  {
    name: 'Cơm Gà Teriyaki',
    description: 'Cơm với gà sốt teriyaki, rau củ xào',
    category: 'Cơm'
  },
  {
    name: 'Cơm Rang Thập Cẩm',
    description: 'Cơm rang với tôm, thịt, trứng, rau củ đầy đủ',
    category: 'Cơm'
  },
  {
    name: 'Cơm Niêu',
    description: 'Cơm niêu Sài Gòn với cá kho, thịt kho, trứng',
    category: 'Cơm'
  },
  {
    name: 'Cơm Hến',
    description: 'Cơm hến Huế với hến xào, rau thơm, nước mắm',
    category: 'Cơm'
  },
  {
    name: 'Cơm Hải Sản',
    description: 'Cơm chiên hải sản với tôm, mực, cua',
    category: 'Cơm'
  },
  {
    name: 'Cơm Cá Kho',
    description: 'Cơm với cá kho tộ đậm đà, canh chua',
    category: 'Cơm'
  },
  {
    name: 'Cơm Bò Lúc Lắc',
    description: 'Cơm với bò lúc lắc mềm, khoai tây chiên',
    category: 'Cơm'
  },
  {
    name: 'Cơm Chiên Hải Sản',
    description: 'Cơm chiên với tôm, mực, nghêu tươi ngon',
    category: 'Cơm'
  },

  // Cà Phê (8 products)
  {
    name: 'Cà Phê Đen Đá',
    description: 'Cà phê phin truyền thống, đậm đà, đắng nhẹ',
    category: 'Cà Phê'
  },
  {
    name: 'Cà Phê Sữa Đá',
    description: 'Cà phê phin pha sữa đặc, ngọt ngào, thơm béo',
    category: 'Cà Phê'
  },
  {
    name: 'Bạc Xỉu',
    description: 'Cà phê sữa nhiều sữa, ít cà phê, ngọt dịu',
    category: 'Cà Phê'
  },
  {
    name: 'Cà Phê Trứng',
    description: 'Cà phê Hà Nội với lớp kem trứng béo ngậy',
    category: 'Cà Phê'
  },
  {
    name: 'Cà Phê Dừa',
    description: 'Cà phê pha nước cốt dừa, thơm mát, độc đáo',
    category: 'Cà Phê'
  },
  {
    name: 'Espresso',
    description: 'Cà phê espresso đậm đà, pha máy chuyên nghiệp',
    category: 'Cà Phê'
  },
  {
    name: 'Cappuccino',
    description: 'Cà phê cappuccino với lớp sữa foam mịn màng',
    category: 'Cà Phê'
  },
  {
    name: 'Latte',
    description: 'Cà phê latte với nhiều sữa tươi, vị nhẹ nhàng',
    category: 'Cà Phê'
  },

  // Trà Sữa (12 products)
  {
    name: 'Trà Sữa Trân Châu Đường Đen',
    description: 'Trà sữa với trân châu đường đen dai mềm, vị ngọt tự nhiên',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Matcha',
    description: 'Trà sữa matcha Nhật Bản, vị đắng nhẹ, thơm béo',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Socola',
    description: 'Trà sữa socola đậm đà, ngọt ngào',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Khoai Môn',
    description: 'Trà sữa khoai môn béo ngậy, thơm mùi khoai',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Dâu',
    description: 'Trà sữa dâu tươi, vị chua ngọt hài hòa',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Đào',
    description: 'Trà sữa đào thơm mát, ngọt dịu',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Ô Long',
    description: 'Trà sữa ô long truyền thống, thơm nhẹ',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Thái Xanh',
    description: 'Trà sữa Thái xanh đặc trưng, màu xanh đẹp mắt',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Caramel',
    description: 'Trà sữa caramel ngọt béo, thơm mùi bơ',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Hokkaido',
    description: 'Trà sữa Hokkaido với sữa tươi Nhật Bản',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Phô Mai',
    description: 'Trà sữa phô mai mặn mặn, béo ngậy',
    category: 'Trà Sữa'
  },
  {
    name: 'Trà Sữa Truyền Thống',
    description: 'Trà sữa truyền thống Đài Loan, vị chuẩn',
    category: 'Trà Sữa'
  },

  // Nước Ép (10 products)
  {
    name: 'Nước Ép Cam',
    description: 'Nước cam tươi nguyên chất, giàu vitamin C',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Dưa Hấu',
    description: 'Nước dưa hấu mát lạnh, ngọt tự nhiên',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Táo',
    description: 'Nước táo tươi, giòn ngọt, bổ dưỡng',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Cà Rốt',
    description: 'Nước cà rốt giàu vitamin A, tốt cho mắt',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Dứa',
    description: 'Nước dứa tươi, chua ngọt, giải nhiệt',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Ổi',
    description: 'Nước ổi tươi, giàu vitamin C, thơm ngon',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Bưởi',
    description: 'Nước bưởi tươi, đắng nhẹ, giải nhiệt',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Chanh',
    description: 'Nước chanh tươi, chua chua, giải khát',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Dâu',
    description: 'Nước dâu tươi, màu đỏ đẹp, ngọt dịu',
    category: 'Nước Ép'
  },
  {
    name: 'Nước Ép Thập Cẩm',
    description: 'Nước ép hỗn hợp nhiều loại trái cây',
    category: 'Nước Ép'
  },

  // Sinh Tố (10 products)
  {
    name: 'Sinh Tố Bơ',
    description: 'Sinh tố bơ béo ngậy, bổ dưỡng',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Mãng Cầu',
    description: 'Sinh tố mãng cầu thơm ngọt, mát lạnh',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Dâu',
    description: 'Sinh tố dâu tươi, màu hồng đẹp mắt',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Xoài',
    description: 'Sinh tố xoài ngọt lịm, thơm nồng',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Sapoche',
    description: 'Sinh tố sapoche béo ngậy, ngọt tự nhiên',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Dừa',
    description: 'Sinh tố dừa mát lạnh, thơm béo',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Chuối',
    description: 'Sinh tố chuối bổ dưỡng, giàu kali',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Dưa Hấu',
    description: 'Sinh tố dưa hấu mát lạnh, giải nhiệt',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Thập Cẩm',
    description: 'Sinh tố hỗn hợp nhiều loại trái cây',
    category: 'Sinh Tố'
  },
  {
    name: 'Sinh Tố Bơ Sữa Chua',
    description: 'Sinh tố bơ pha sữa chua, béo ngậy, chua nhẹ',
    category: 'Sinh Tố'
  },

  // Trà Trái Cây (8 products)
  {
    name: 'Trà Đào Cam Sả',
    description: 'Trà đào cam sả thơm mát, chua ngọt hài hòa',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Chanh Leo',
    description: 'Trà chanh leo chua chua, thơm mát',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Vải',
    description: 'Trà vải ngọt dịu, thơm mùi vải',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Dâu',
    description: 'Trà dâu tươi, màu hồng đẹp, ngọt nhẹ',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Xoài',
    description: 'Trà xoài thơm ngọt, mát lạnh',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Thanh Long',
    description: 'Trà thanh long ruột đỏ, đẹp mắt, ngọt dịu',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Dứa',
    description: 'Trà dứa chua ngọt, giải nhiệt',
    category: 'Trà Trái Cây'
  },
  {
    name: 'Trà Thập Cẩm',
    description: 'Trà hỗn hợp nhiều loại trái cây tươi',
    category: 'Trà Trái Cây'
  },

  // Chè (8 products)
  {
    name: 'Chè Thái',
    description: 'Chè Thái với nhiều loại topping, nước cốt dừa',
    category: 'Chè'
  },
  {
    name: 'Chè Khúc Bạch',
    description: 'Chè khúc bạch mát lạnh, mềm mịn',
    category: 'Chè'
  },
  {
    name: 'Chè Bưởi',
    description: 'Chè bưởi thơm, ngọt dịu, mát lạnh',
    category: 'Chè'
  },
  {
    name: 'Chè Đậu Đỏ',
    description: 'Chè đậu đỏ truyền thống, bổ dưỡng',
    category: 'Chè'
  },
  {
    name: 'Chè Sương Sa Hột Lựu',
    description: 'Chè sương sa hột lựu mát lạnh, dai giòn',
    category: 'Chè'
  },
  {
    name: 'Chè Trôi Nước',
    description: 'Chè trôi nước Hà Nội, bánh trôi mềm, nước gừng thơm',
    category: 'Chè'
  },
  {
    name: 'Chè Bà Ba',
    description: 'Chè bà ba miền Tây, nhiều màu sắc',
    category: 'Chè'
  },
  {
    name: 'Chè Thập Cẩm',
    description: 'Chè thập cẩm với nhiều loại đậu, thạch',
    category: 'Chè'
  },

  // Bánh Mì (8 products)
  {
    name: 'Bánh Mì Thịt',
    description: 'Bánh mì giòn với thịt nguội, pate, rau thơm',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Pate',
    description: 'Bánh mì pate truyền thống Sài Gòn',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Xíu Mại',
    description: 'Bánh mì xíu mại sốt cà chua đậm đà',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Chả Lụa',
    description: 'Bánh mì chả lụa với dưa chua, rau thơm',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Trứng',
    description: 'Bánh mì trứng ốp la, thơm béo',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Gà',
    description: 'Bánh mì gà xé phay, sốt mayonnaise',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Bò Kho',
    description: 'Bánh mì chấm bò kho tiêu thơm nồng',
    category: 'Bánh Mì'
  },
  {
    name: 'Bánh Mì Đặc Biệt',
    description: 'Bánh mì đầy đủ với thịt, pate, chả, trứng',
    category: 'Bánh Mì'
  },

  // Lẩu (10 products)
  {
    name: 'Lẩu Thái Hải Sản',
    description: 'Lẩu Thái chua cay với tôm, mực, cá tươi ngon',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Thái Gà',
    description: 'Lẩu Thái gà chua cay, nước dùng đậm đà',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Thái Bò',
    description: 'Lẩu Thái bò với thịt bò tươi, rau củ',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Hải Sản Chua Cay',
    description: 'Lẩu hải sản chua cay kiểu Việt, nước dùng thơm ngon',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Hải Sản Nấm',
    description: 'Lẩu hải sản nấm thanh ngọt, bổ dưỡng',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Bò Nhúng Dấm',
    description: 'Lẩu bò nhúng dấm miền Trung, chua chua, thơm mùi mè',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Bò Mỹ',
    description: 'Lẩu bò Mỹ với thịt bò cao cấp, nước dùng ngọt',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Bò Nấm',
    description: 'Lẩu bò nấm thanh ngọt, nhiều loại nấm',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Gà Lá É',
    description: 'Lẩu gà lá é miền Tây, thơm mùi lá é',
    category: 'Lẩu'
  },
  {
    name: 'Lẩu Cá Kèo',
    description: 'Lẩu cá kèo miền Tây, nước dùng ngọt thanh',
    category: 'Lẩu'
  },

  // Món Nướng (10 products)
  {
    name: 'Sườn Nướng BBQ',
    description: 'Sườn nướng BBQ thơm nồng, sốt đậm đà',
    category: 'Món Nướng'
  },
  {
    name: 'Thịt Ba Chỉ Nướng',
    description: 'Thịt ba chỉ nướng giòn rụm, béo ngậy',
    category: 'Món Nướng'
  },
  {
    name: 'Thịt Xiên Nướng',
    description: 'Thịt xiên nướng thơm, ăn kèm rau sống',
    category: 'Món Nướng'
  },
  {
    name: 'Sườn Non Nướng Mật Ong',
    description: 'Sườn non nướng mật ong ngọt béo, mềm',
    category: 'Món Nướng'
  },
  {
    name: 'Tôm Nướng Muối Ớt',
    description: 'Tôm nướng muối ớt thơm cay, giòn tan',
    category: 'Món Nướng'
  },
  {
    name: 'Mực Nướng Sa Tế',
    description: 'Mực nướng sa tế cay nồng, thơm ngon',
    category: 'Món Nướng'
  },
  {
    name: 'Cá Nướng Muối Ớt',
    description: 'Cá nướng muối ớt thơm, giòn ngoài mềm trong',
    category: 'Món Nướng'
  },
  {
    name: 'Sò Điệp Nướng Mỡ Hành',
    description: 'Sò điệp nướng mỡ hành thơm béo, ngọt thịt',
    category: 'Món Nướng'
  },
  {
    name: 'Nghêu Nướng Phô Mai',
    description: 'Nghêu nướng phô mai béo ngậy, thơm mùi phô mai',
    category: 'Món Nướng'
  },
  {
    name: 'Bạch Tuộc Nướng',
    description: 'Bạch tuộc nướng giòn, chấm wasabi',
    category: 'Món Nướng'
  },

  // Món Ăn Vặt (10 products)
  {
    name: 'Nem Chua Rán',
    description: 'Nem chua rán giòn rụm, chua chua, thơm ngon',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Chả Giò Rế',
    description: 'Chả giò rế miền Nam, giòn tan, nhân thịt đầy đặn',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Chả Giò Hải Sản',
    description: 'Chả giò hải sản với tôm, cua, mực',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Bánh Tráng Trộn Khô Bò',
    description: 'Bánh tráng trộn khô bò cay, giòn, thơm',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Bánh Tráng Trộn Tôm',
    description: 'Bánh tráng trộn tôm khô, trứng cút, rau răm',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Bánh Bao Nhân Thịt',
    description: 'Bánh bao nhân thịt mềm xốp, nhân đầy đặn',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Bánh Bao Nhân Đậu Xanh',
    description: 'Bánh bao nhân đậu xanh ngọt dịu, mềm mịn',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Bánh Bao Kim Sa',
    description: 'Bánh bao kim sa nhân trứng muối béo ngậy',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Há Cảo',
    description: 'Há cảo tôm thịt, vỏ mỏng, nhân ngọt',
    category: 'Món Ăn Vặt'
  },
  {
    name: 'Xíu Mại',
    description: 'Xíu mại sốt cà chua đậm đà, thơm ngon',
    category: 'Món Ăn Vặt'
  },

  // Đồ Ăn Nhanh (10 products)
  {
    name: 'Burger Bò',
    description: 'Burger bò với thịt bò xay, rau xà lách, cà chua',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Burger Gà',
    description: 'Burger gà giòn với sốt mayonnaise',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Burger Phô Mai',
    description: 'Burger phô mai béo ngậy, nhiều lớp phô mai',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Pizza Hải Sản',
    description: 'Pizza hải sản với tôm, mực, nghêu',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Pizza Xúc Xích',
    description: 'Pizza xúc xích với nhiều xúc xích, phô mai',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Pizza Thập Cẩm',
    description: 'Pizza thập cẩm với nhiều loại topping',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Gà Rán Giòn',
    description: 'Gà rán giòn rụm, thơm mùi gia vị',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Gà Rán Cay',
    description: 'Gà rán cay nồng, giòn tan',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Gà Rán Phô Mai',
    description: 'Gà rán phủ phô mai béo ngậy',
    category: 'Đồ Ăn Nhanh'
  },
  {
    name: 'Khoai Tây Chiên',
    description: 'Khoai tây chiên giòn, ăn kèm sốt',
    category: 'Đồ Ăn Nhanh'
  },

  // Nem & Chả (8 products)
  {
    name: 'Nem Nướng Nha Trang',
    description: 'Nem nướng Nha Trang thơm, ăn kèm bánh tráng',
    category: 'Nem & Chả'
  },
  {
    name: 'Nem Cuốn',
    description: 'Nem cuốn tươi với tôm, thịt, rau sống',
    category: 'Nem & Chả'
  },
  {
    name: 'Nem Rán',
    description: 'Nem rán giòn rụm, nhân thịt đầy đặn',
    category: 'Nem & Chả'
  },
  {
    name: 'Chả Giò',
    description: 'Chả giò miền Nam, giòn tan, thơm ngon',
    category: 'Nem & Chả'
  },
  {
    name: 'Chả Lụa',
    description: 'Chả lụa Hà Nội truyền thống, mềm mịn',
    category: 'Nem & Chả'
  },
  {
    name: 'Chả Cá',
    description: 'Chả cá Lã Vọng với thì là, mắm tôm',
    category: 'Nem & Chả'
  },
  {
    name: 'Nem Lui',
    description: 'Nem lui Huế với thịt nướng xiên que sả',
    category: 'Nem & Chả'
  },
  {
    name: 'Chả Ram',
    description: 'Chả ram tôm đất miền Tây, giòn thơm',
    category: 'Nem & Chả'
  },

  // Món Hải Sản (10 products)
  {
    name: 'Tôm Hấp Bia',
    description: 'Tôm hấp bia thơm, ngọt thịt, tươi ngon',
    category: 'Món Hải Sản'
  },
  {
    name: 'Cua Rang Me',
    description: 'Cua rang me chua chua, ngọt thịt cua',
    category: 'Món Hải Sản'
  },
  {
    name: 'Mực Xào Sa Tế',
    description: 'Mực xào sa tế cay nồng, giòn dai',
    category: 'Món Hải Sản'
  },
  {
    name: 'Nghêu Hấp Xả',
    description: 'Nghêu hấp xả thơm mùi sả, ngọt thịt',
    category: 'Món Hải Sản'
  },
  {
    name: 'Ốc Hương Xào Bơ Tỏi',
    description: 'Ốc hương xào bơ tỏi thơm béo, dai giòn',
    category: 'Món Hải Sản'
  },
  {
    name: 'Cá Lóc Kho Tộ',
    description: 'Cá lóc kho tộ đậm đà, thơm mùi nước mắm',
    category: 'Món Hải Sản'
  },
  {
    name: 'Cá Diêu Hồng Chiên',
    description: 'Cá diêu hồng chiên giòn, chấm mắm gừng',
    category: 'Món Hải Sản'
  },
  {
    name: 'Sò Điệp Hấp',
    description: 'Sò điệp hấp tươi ngon, ngọt thịt',
    category: 'Món Hải Sản'
  },
  {
    name: 'Bạch Tuộc Xào',
    description: 'Bạch tuộc xào giòn dai, thơm mùi gia vị',
    category: 'Món Hải Sản'
  },
  {
    name: 'Ghẹ Rang Muối',
    description: 'Ghẹ rang muối thơm cay, giòn vỏ',
    category: 'Món Hải Sản'
  },

  // Xôi (6 products)
  {
    name: 'Xôi Xéo',
    description: 'Xôi xéo Hà Nội với đậu xanh, hành phi',
    category: 'Xôi'
  },
  {
    name: 'Xôi Gà',
    description: 'Xôi gà với thịt gà xé, hành phi thơm',
    category: 'Xôi'
  },
  {
    name: 'Xôi Lạc',
    description: 'Xôi lạc với đậu phộng rang giòn',
    category: 'Xôi'
  },
  {
    name: 'Xôi Thập Cẩm',
    description: 'Xôi thập cẩm với nhiều loại topping',
    category: 'Xôi'
  },
  {
    name: 'Xôi Chả',
    description: 'Xôi chả với chả lụa, giò, pate',
    category: 'Xôi'
  },
  {
    name: 'Xôi Đậu Xanh',
    description: 'Xôi đậu xanh mềm dẻo, thơm béo',
    category: 'Xôi'
  },

  // Cháo (5 products)
  {
    name: 'Cháo Gà',
    description: 'Cháo gà với thịt gà xé, gừng thái sợi',
    category: 'Cháo'
  },
  {
    name: 'Cháo Lòng',
    description: 'Cháo lòng với lòng heo, tiêu thơm',
    category: 'Cháo'
  },
  {
    name: 'Cháo Hải Sản',
    description: 'Cháo hải sản với tôm, mực, cua',
    category: 'Cháo'
  },
  {
    name: 'Cháo Thịt Bằm',
    description: 'Cháo thịt bằm với trứng, hành phi',
    category: 'Cháo'
  },
  {
    name: 'Cháo Cá',
    description: 'Cháo cá với cá lóc, gừng, tiêu',
    category: 'Cháo'
  }
];

/**
 * Get products by category
 */
export function getProductsByCategory(category: string): ProductTemplate[] {
  return PRODUCT_TEMPLATES.filter(p => p.category === category);
}

/**
 * Get all unique categories
 */
export function getAllCategories(): string[] {
  return Array.from(new Set(PRODUCT_TEMPLATES.map(p => p.category)));
}

/**
 * Get product count by category
 */
export function getProductCountByCategory(): Record<string, number> {
  const counts: Record<string, number> = {};
  PRODUCT_TEMPLATES.forEach(p => {
    counts[p.category] = (counts[p.category] || 0) + 1;
  });
  return counts;
}

/**
 * Get random product from category
 */
export function getRandomProductFromCategory(category: string): ProductTemplate | undefined {
  const products = getProductsByCategory(category);
  if (products.length === 0) return undefined;
  return products[Math.floor(Math.random() * products.length)];
}

/**
 * Statistics
 */
export const PRODUCT_STATS = {
  totalProducts: PRODUCT_TEMPLATES.length,
  categories: getAllCategories().length,
  productsByCategory: getProductCountByCategory()
};
