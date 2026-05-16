/**
 * Category Templates for Vietnamese Food E-commerce
 * 
 * Defines 15 root categories and 40+ child categories with parent relationships.
 * Maximum hierarchy depth: 2 levels (root and child only)
 */

export interface CategoryTemplate {
  category_name: string;
  description: string;
  slug: string;
  parent_id: string | null;
  category_group_code: string;
  is_active: boolean;
}

/**
 * Root Categories (15 categories)
 */
export const ROOT_CATEGORIES: CategoryTemplate[] = [
  {
    category_name: 'Món Việt Truyền Thống',
    description: 'Các món ăn truyền thống Việt Nam đậm đà bản sắc dân tộc',
    slug: 'mon-viet-truyen-thong',
    parent_id: null,
    category_group_code: 'VIETNAMESE',
    is_active: true
  },
  {
    category_name: 'Đồ Uống',
    description: 'Các loại đồ uống từ trà, cà phê đến nước ép tươi mát',
    slug: 'do-uong',
    parent_id: null,
    category_group_code: 'BEVERAGES',
    is_active: true
  },
  {
    category_name: 'Tráng Miệng',
    description: 'Các món tráng miệng ngọt ngào, thanh mát',
    slug: 'trang-mieng',
    parent_id: null,
    category_group_code: 'DESSERTS',
    is_active: true
  },
  {
    category_name: 'Món Chay',
    description: 'Các món ăn chay thanh đạm, bổ dưỡng',
    slug: 'mon-chay',
    parent_id: null,
    category_group_code: 'VEGETARIAN',
    is_active: true
  },
  {
    category_name: 'Món Hải Sản',
    description: 'Các món hải sản tươi ngon từ biển cả',
    slug: 'mon-hai-san',
    parent_id: null,
    category_group_code: 'SEAFOOD',
    is_active: true
  },
  {
    category_name: 'Món Nướng & BBQ',
    description: 'Các món nướng thơm phức, hấp dẫn',
    slug: 'mon-nuong-bbq',
    parent_id: null,
    category_group_code: 'GRILLED',
    is_active: true
  },
  {
    category_name: 'Món Lẩu',
    description: 'Các loại lẩu đa dạng, phong phú',
    slug: 'mon-lau',
    parent_id: null,
    category_group_code: 'HOTPOT',
    is_active: true
  },
  {
    category_name: 'Món Ăn Sáng',
    description: 'Các món ăn sáng bổ dưỡng, tiện lợi',
    slug: 'mon-an-sang',
    parent_id: null,
    category_group_code: 'BREAKFAST',
    is_active: true
  },
  {
    category_name: 'Món Ăn Vặt',
    description: 'Các món ăn vặt ngon miệng, hấp dẫn',
    slug: 'mon-an-vat',
    parent_id: null,
    category_group_code: 'SNACKS',
    is_active: true
  },
  {
    category_name: 'Đồ Ăn Nhanh',
    description: 'Các món ăn nhanh tiện lợi, nhanh chóng',
    slug: 'do-an-nhanh',
    parent_id: null,
    category_group_code: 'FASTFOOD',
    is_active: true
  },
  {
    category_name: 'Món Á',
    description: 'Các món ăn châu Á đa dạng phong phú',
    slug: 'mon-a',
    parent_id: null,
    category_group_code: 'ASIAN',
    is_active: true
  },
  {
    category_name: 'Món Âu',
    description: 'Các món ăn phương Tây sang trọng, tinh tế',
    slug: 'mon-au',
    parent_id: null,
    category_group_code: 'WESTERN',
    is_active: true
  },
  {
    category_name: 'Bánh Ngọt & Bánh Mì',
    description: 'Các loại bánh ngọt và bánh mì thơm ngon',
    slug: 'banh-ngot-banh-mi',
    parent_id: null,
    category_group_code: 'BAKERY',
    is_active: true
  },
  {
    category_name: 'Món Healthy',
    description: 'Các món ăn lành mạnh, ít calo',
    slug: 'mon-healthy',
    parent_id: null,
    category_group_code: 'HEALTHY',
    is_active: true
  },
  {
    category_name: 'Món Đặc Sản Miền',
    description: 'Các món đặc sản từ ba miền Bắc - Trung - Nam',
    slug: 'mon-dac-san-mien',
    parent_id: null,
    category_group_code: 'REGIONAL',
    is_active: true
  }
];

/**
 * Child Categories (40+ categories)
 * Each child category references a parent category
 */
export const CHILD_CATEGORIES: CategoryTemplate[] = [
  // Món Việt Truyền Thống (5 children)
  {
    category_name: 'Phở',
    description: 'Món phở truyền thống với nước dùng thơm ngon',
    slug: 'pho',
    parent_id: 'Món Việt Truyền Thống',
    category_group_code: 'VIETNAMESE',
    is_active: true
  },
  {
    category_name: 'Bún',
    description: 'Các món bún đa dạng từ Bắc vào Nam',
    slug: 'bun',
    parent_id: 'Món Việt Truyền Thống',
    category_group_code: 'VIETNAMESE',
    is_active: true
  },
  {
    category_name: 'Cơm',
    description: 'Các món cơm phong phú, đa dạng',
    slug: 'com',
    parent_id: 'Món Việt Truyền Thống',
    category_group_code: 'VIETNAMESE',
    is_active: true
  },
  {
    category_name: 'Nem & Chả',
    description: 'Các loại nem và chả truyền thống',
    slug: 'nem-cha',
    parent_id: 'Món Việt Truyền Thống',
    category_group_code: 'VIETNAMESE',
    is_active: true
  },
  {
    category_name: 'Bánh Xèo & Bánh Cuốn',
    description: 'Các loại bánh truyền thống Việt Nam',
    slug: 'banh-xeo-banh-cuon',
    parent_id: 'Món Việt Truyền Thống',
    category_group_code: 'VIETNAMESE',
    is_active: true
  },

  // Đồ Uống (5 children)
  {
    category_name: 'Cà Phê',
    description: 'Các loại cà phê từ truyền thống đến hiện đại',
    slug: 'ca-phe',
    parent_id: 'Đồ Uống',
    category_group_code: 'BEVERAGES',
    is_active: true
  },
  {
    category_name: 'Trà Sữa',
    description: 'Trà sữa đa dạng với nhiều topping hấp dẫn',
    slug: 'tra-sua',
    parent_id: 'Đồ Uống',
    category_group_code: 'BEVERAGES',
    is_active: true
  },
  {
    category_name: 'Trà Trái Cây',
    description: 'Trà trái cây tươi mát, thanh nhiệt',
    slug: 'tra-trai-cay',
    parent_id: 'Đồ Uống',
    category_group_code: 'BEVERAGES',
    is_active: true
  },
  {
    category_name: 'Nước Ép',
    description: 'Nước ép trái cây tươi nguyên chất',
    slug: 'nuoc-ep',
    parent_id: 'Đồ Uống',
    category_group_code: 'BEVERAGES',
    is_active: true
  },
  {
    category_name: 'Sinh Tố',
    description: 'Sinh tố trái cây bổ dưỡng',
    slug: 'sinh-to',
    parent_id: 'Đồ Uống',
    category_group_code: 'BEVERAGES',
    is_active: true
  },

  // Tráng Miệng (4 children)
  {
    category_name: 'Chè',
    description: 'Các loại chè truyền thống Việt Nam',
    slug: 'che',
    parent_id: 'Tráng Miệng',
    category_group_code: 'DESSERTS',
    is_active: true
  },
  {
    category_name: 'Kem',
    description: 'Kem các loại mát lạnh, ngọt ngào',
    slug: 'kem',
    parent_id: 'Tráng Miệng',
    category_group_code: 'DESSERTS',
    is_active: true
  },
  {
    category_name: 'Bánh Ngọt',
    description: 'Các loại bánh ngọt thơm ngon',
    slug: 'banh-ngot',
    parent_id: 'Tráng Miệng',
    category_group_code: 'DESSERTS',
    is_active: true
  },
  {
    category_name: 'Trái Cây Dầm',
    description: 'Trái cây tươi ngon với nước cốt dừa',
    slug: 'trai-cay-dam',
    parent_id: 'Tráng Miệng',
    category_group_code: 'DESSERTS',
    is_active: true
  },

  // Món Chay (3 children)
  {
    category_name: 'Cơm Chay',
    description: 'Các món cơm chay thanh đạm',
    slug: 'com-chay',
    parent_id: 'Món Chay',
    category_group_code: 'VEGETARIAN',
    is_active: true
  },
  {
    category_name: 'Bún Chay',
    description: 'Các món bún chay bổ dưỡng',
    slug: 'bun-chay',
    parent_id: 'Món Chay',
    category_group_code: 'VEGETARIAN',
    is_active: true
  },
  {
    category_name: 'Lẩu Chay',
    description: 'Lẩu chay thanh đạm, bổ dưỡng',
    slug: 'lau-chay',
    parent_id: 'Món Chay',
    category_group_code: 'VEGETARIAN',
    is_active: true
  },

  // Món Hải Sản (3 children)
  {
    category_name: 'Hải Sản Nướng',
    description: 'Các món hải sản nướng thơm phức',
    slug: 'hai-san-nuong',
    parent_id: 'Món Hải Sản',
    category_group_code: 'SEAFOOD',
    is_active: true
  },
  {
    category_name: 'Hải Sản Hấp',
    description: 'Hải sản hấp giữ nguyên vị ngọt tự nhiên',
    slug: 'hai-san-hap',
    parent_id: 'Món Hải Sản',
    category_group_code: 'SEAFOOD',
    is_active: true
  },
  {
    category_name: 'Hải Sản Chiên',
    description: 'Hải sản chiên giòn rụm, thơm ngon',
    slug: 'hai-san-chien',
    parent_id: 'Món Hải Sản',
    category_group_code: 'SEAFOOD',
    is_active: true
  },

  // Món Nướng & BBQ (3 children)
  {
    category_name: 'Thịt Nướng',
    description: 'Các món thịt nướng BBQ đậm đà',
    slug: 'thit-nuong',
    parent_id: 'Món Nướng & BBQ',
    category_group_code: 'GRILLED',
    is_active: true
  },
  {
    category_name: 'Hải Sản Nướng BBQ',
    description: 'Hải sản nướng BBQ thơm lừng',
    slug: 'hai-san-nuong-bbq',
    parent_id: 'Món Nướng & BBQ',
    category_group_code: 'GRILLED',
    is_active: true
  },
  {
    category_name: 'Xiên Nướng',
    description: 'Các loại xiên nướng đa dạng',
    slug: 'xien-nuong',
    parent_id: 'Món Nướng & BBQ',
    category_group_code: 'GRILLED',
    is_active: true
  },

  // Món Lẩu (3 children)
  {
    category_name: 'Lẩu Thái',
    description: 'Lẩu Thái chua cay đặc trưng',
    slug: 'lau-thai',
    parent_id: 'Món Lẩu',
    category_group_code: 'HOTPOT',
    is_active: true
  },
  {
    category_name: 'Lẩu Hải Sản',
    description: 'Lẩu hải sản tươi ngon, bổ dưỡng',
    slug: 'lau-hai-san',
    parent_id: 'Món Lẩu',
    category_group_code: 'HOTPOT',
    is_active: true
  },
  {
    category_name: 'Lẩu Bò',
    description: 'Lẩu bò thơm ngon, đậm đà',
    slug: 'lau-bo',
    parent_id: 'Món Lẩu',
    category_group_code: 'HOTPOT',
    is_active: true
  },

  // Món Ăn Sáng (3 children)
  {
    category_name: 'Bánh Mì',
    description: 'Bánh mì Việt Nam đa dạng nhân',
    slug: 'banh-mi',
    parent_id: 'Món Ăn Sáng',
    category_group_code: 'BREAKFAST',
    is_active: true
  },
  {
    category_name: 'Xôi',
    description: 'Xôi nóng hổi, bổ dưỡng',
    slug: 'xoi',
    parent_id: 'Món Ăn Sáng',
    category_group_code: 'BREAKFAST',
    is_active: true
  },
  {
    category_name: 'Cháo',
    description: 'Cháo nóng hổi, dễ ăn',
    slug: 'chao',
    parent_id: 'Món Ăn Sáng',
    category_group_code: 'BREAKFAST',
    is_active: true
  },

  // Món Ăn Vặt (3 children)
  {
    category_name: 'Nem Rán',
    description: 'Nem rán giòn rụm, thơm ngon',
    slug: 'nem-ran',
    parent_id: 'Món Ăn Vặt',
    category_group_code: 'SNACKS',
    is_active: true
  },
  {
    category_name: 'Bánh Tráng Trộn',
    description: 'Bánh tráng trộn cay nồng, hấp dẫn',
    slug: 'banh-trang-tron',
    parent_id: 'Món Ăn Vặt',
    category_group_code: 'SNACKS',
    is_active: true
  },
  {
    category_name: 'Bánh Bao',
    description: 'Bánh bao nhân đa dạng, thơm ngon',
    slug: 'banh-bao',
    parent_id: 'Món Ăn Vặt',
    category_group_code: 'SNACKS',
    is_active: true
  },

  // Đồ Ăn Nhanh (3 children)
  {
    category_name: 'Burger',
    description: 'Burger thơm ngon, đầy đặn',
    slug: 'burger',
    parent_id: 'Đồ Ăn Nhanh',
    category_group_code: 'FASTFOOD',
    is_active: true
  },
  {
    category_name: 'Pizza',
    description: 'Pizza đa dạng topping, thơm phức',
    slug: 'pizza',
    parent_id: 'Đồ Ăn Nhanh',
    category_group_code: 'FASTFOOD',
    is_active: true
  },
  {
    category_name: 'Gà Rán',
    description: 'Gà rán giòn tan, đậm vị',
    slug: 'ga-ran',
    parent_id: 'Đồ Ăn Nhanh',
    category_group_code: 'FASTFOOD',
    is_active: true
  },

  // Món Á (3 children)
  {
    category_name: 'Sushi',
    description: 'Sushi Nhật Bản tươi ngon',
    slug: 'sushi',
    parent_id: 'Món Á',
    category_group_code: 'ASIAN',
    is_active: true
  },
  {
    category_name: 'Dimsum',
    description: 'Dimsum Trung Hoa đa dạng',
    slug: 'dimsum',
    parent_id: 'Món Á',
    category_group_code: 'ASIAN',
    is_active: true
  },
  {
    category_name: 'Mì Ý',
    description: 'Mì Ý với sốt đậm đà',
    slug: 'mi-y',
    parent_id: 'Món Á',
    category_group_code: 'ASIAN',
    is_active: true
  },

  // Món Âu (3 children)
  {
    category_name: 'Pasta',
    description: 'Pasta Ý với nhiều loại sốt',
    slug: 'pasta',
    parent_id: 'Món Âu',
    category_group_code: 'WESTERN',
    is_active: true
  },
  {
    category_name: 'Steak',
    description: 'Steak bò cao cấp, mềm ngon',
    slug: 'steak',
    parent_id: 'Món Âu',
    category_group_code: 'WESTERN',
    is_active: true
  },
  {
    category_name: 'Salad',
    description: 'Salad tươi mát, bổ dưỡng',
    slug: 'salad',
    parent_id: 'Món Âu',
    category_group_code: 'WESTERN',
    is_active: true
  },

  // Bánh Ngọt & Bánh Mì (3 children)
  {
    category_name: 'Bánh Mì Ngọt',
    description: 'Bánh mì ngọt mềm mịn, thơm ngon',
    slug: 'banh-mi-ngot',
    parent_id: 'Bánh Ngọt & Bánh Mì',
    category_group_code: 'BAKERY',
    is_active: true
  },
  {
    category_name: 'Bánh Kem',
    description: 'Bánh kem ngọt ngào, đẹp mắt',
    slug: 'banh-kem',
    parent_id: 'Bánh Ngọt & Bánh Mì',
    category_group_code: 'BAKERY',
    is_active: true
  },
  {
    category_name: 'Bánh Bông Lan',
    description: 'Bánh bông lan mềm xốp, thơm bơ',
    slug: 'banh-bong-lan',
    parent_id: 'Bánh Ngọt & Bánh Mì',
    category_group_code: 'BAKERY',
    is_active: true
  },

  // Món Healthy (3 children)
  {
    category_name: 'Salad Healthy',
    description: 'Salad ít calo, nhiều dinh dưỡng',
    slug: 'salad-healthy',
    parent_id: 'Món Healthy',
    category_group_code: 'HEALTHY',
    is_active: true
  },
  {
    category_name: 'Smoothie Bowl',
    description: 'Smoothie bowl tươi mát, bổ dưỡng',
    slug: 'smoothie-bowl',
    parent_id: 'Món Healthy',
    category_group_code: 'HEALTHY',
    is_active: true
  },
  {
    category_name: 'Ức Gà',
    description: 'Ức gà ít béo, nhiều protein',
    slug: 'uc-ga',
    parent_id: 'Món Healthy',
    category_group_code: 'HEALTHY',
    is_active: true
  },

  // Món Đặc Sản Miền (3 children)
  {
    category_name: 'Miền Bắc',
    description: 'Các món đặc sản miền Bắc',
    slug: 'mien-bac',
    parent_id: 'Món Đặc Sản Miền',
    category_group_code: 'REGIONAL',
    is_active: true
  },
  {
    category_name: 'Miền Trung',
    description: 'Các món đặc sản miền Trung',
    slug: 'mien-trung',
    parent_id: 'Món Đặc Sản Miền',
    category_group_code: 'REGIONAL',
    is_active: true
  },
  {
    category_name: 'Miền Nam',
    description: 'Các món đặc sản miền Nam',
    slug: 'mien-nam',
    parent_id: 'Món Đặc Sản Miền',
    category_group_code: 'REGIONAL',
    is_active: true
  }
];

/**
 * Get all categories (root + children)
 */
export function getAllCategories(): CategoryTemplate[] {
  return [...ROOT_CATEGORIES, ...CHILD_CATEGORIES];
}

/**
 * Get categories by parent
 */
export function getCategoriesByParent(parentName: string | null): CategoryTemplate[] {
  if (parentName === null) {
    return ROOT_CATEGORIES;
  }
  return CHILD_CATEGORIES.filter(cat => cat.parent_id === parentName);
}

/**
 * Get category by name
 */
export function getCategoryByName(name: string): CategoryTemplate | undefined {
  return getAllCategories().find(cat => cat.category_name === name);
}

/**
 * Validate category hierarchy depth (max 2 levels)
 */
export function validateCategoryHierarchy(): boolean {
  // Check that no child category has children
  for (const child of CHILD_CATEGORIES) {
    const hasChildren = CHILD_CATEGORIES.some(c => c.parent_id === child.category_name);
    if (hasChildren) {
      console.error(`Category hierarchy violation: ${child.category_name} has children but is already a child category`);
      return false;
    }
  }
  return true;
}
