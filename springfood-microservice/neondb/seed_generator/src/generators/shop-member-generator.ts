/**
 * Shop Member Generator Module
 * 
 * Generates shop employees (2-5 per shop) with new user accounts.
 * Each shop gets Manager, Staff, Cashier, and Delivery roles.
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { hashPassword } from '../utils/bcrypt-hasher';

export interface ShopMember {
  shop_member_id: string;
  shop_id: string;
  user_id: string;
  role_name: string;
  department: string;
  join_date: string;
  status: string;
  end_date: string | null;
  work_schedule: string;
  salary_type: string;
  base_salary: number;
  commission: number;
  created_at: Date;
  updated_at: Date;
}

export interface ShopEmployee {
  user_id: string;
  first_name: string;
  last_name: string;
  email: string;
  phone: string;
  password: string;
  avatar: string;
  shop_id: string | null;
  status: string;
  email_verified: boolean;
  phone_verified: boolean;
  created_at: Date;
  updated_at: Date;
  role: string; // For user_has_role junction
}

/**
 * Vietnamese employee names
 */
const EMPLOYEE_FIRST_NAMES = {
  male: ['Văn', 'Đức', 'Minh', 'Hoàng', 'Quang', 'Tuấn', 'Hải', 'Thành', 'Dũng', 'Khoa', 'Long', 'Nam', 'Phong', 'Tài', 'Vinh'],
  female: ['Thị', 'Thu', 'Hương', 'Lan', 'Mai', 'Linh', 'Nga', 'Hà', 'Trang', 'Phương', 'Anh', 'Chi', 'Dung', 'Hoa', 'Nhung']
};

const EMPLOYEE_LAST_NAMES = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý'];

/**
 * Shop member roles with salary ranges
 */
const SHOP_ROLES = [
  { name: 'Manager', department: 'Management', baseSalary: [15000000, 25000000], commission: [0.05, 0.10] },
  { name: 'Staff', department: 'Operations', baseSalary: [8000000, 12000000], commission: [0.02, 0.05] },
  { name: 'Cashier', department: 'Finance', baseSalary: [7000000, 10000000], commission: [0.01, 0.03] },
  { name: 'Delivery', department: 'Logistics', baseSalary: [6000000, 9000000], commission: [0.03, 0.06] }
];

/**
 * Work schedules
 */
const WORK_SCHEDULES = [
  'Mon-Fri 8:00-17:00',
  'Mon-Sat 9:00-18:00',
  'Tue-Sun 10:00-19:00',
  'Mon-Sun 8:00-12:00',
  'Flexible'
];

/**
 * Generate Vietnamese phone number
 */
function generatePhoneNumber(): string {
  const prefixes = ['090', '091', '093', '094', '097', '098', '086', '096', '032', '033', '034', '035', '036', '037', '038', '039'];
  const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
  const middle = Math.floor(Math.random() * 900 + 100);
  const last = Math.floor(Math.random() * 9000 + 1000);
  return `${prefix}${middle}${last}`;
}

/**
 * Generate avatar URL
 */
function generateAvatarURL(index: number): string {
  return `https://i.pravatar.cc/300?img=${index}`;
}

/**
 * Generate join date (within last 2 years)
 */
function generateJoinDate(): string {
  const now = new Date();
  const twoYearsAgo = new Date(now.getTime() - 2 * 365 * 24 * 60 * 60 * 1000);
  const randomTime = twoYearsAgo.getTime() + Math.random() * (now.getTime() - twoYearsAgo.getTime());
  const joinDate = new Date(randomTime);
  return joinDate.toISOString().split('T')[0]; // YYYY-MM-DD format
}

/**
 * Generate shop members and their user accounts
 * 
 * @param registry - ID registry containing shop IDs
 * @returns Object with shopMembers array and employeeUsers array
 */
export function generateShopMembers(registry: IDRegistry): {
  shopMembers: ShopMember[];
  employeeUsers: ShopEmployee[];
} {
  const now = new Date();
  const shopMembers: ShopMember[] = [];
  const employeeUsers: ShopEmployee[] = [];
  const shopIds = registry.getAllIds('shops');
  
  if (shopIds.length === 0) {
    throw new Error('No shops found in registry. Generate shops first.');
  }

  // Hash password once (same for all employees)
  const hashedPassword = hashPassword('Employee123!');
  
  let employeeCounter = 1;
  let avatarIndex = 20; // Start after existing users

  for (const shopId of shopIds) {
    const shop = registry.getData('shops', shopId);
    
    // Each shop gets 2-5 employees
    const employeeCount = Math.floor(Math.random() * 4) + 2; // 2-5
    
    // Select random roles for this shop
    const selectedRoles = [...SHOP_ROLES]
      .sort(() => Math.random() - 0.5)
      .slice(0, employeeCount);

    for (const roleConfig of selectedRoles) {
      const userId = generateUUID();
      const shopMemberId = generateUUID();
      
      // Generate employee name
      const gender = Math.random() > 0.5 ? 'male' : 'female';
      const firstName = EMPLOYEE_FIRST_NAMES[gender][Math.floor(Math.random() * EMPLOYEE_FIRST_NAMES[gender].length)];
      const lastName = EMPLOYEE_LAST_NAMES[Math.floor(Math.random() * EMPLOYEE_LAST_NAMES.length)];
      
      // Generate email: employee{number}@springfood.vn
      const email = `employee${employeeCounter}@springfood.vn`;
      employeeCounter++;

      // Create user account for employee
      const employeeUser: ShopEmployee = {
        user_id: userId,
        first_name: firstName,
        last_name: lastName,
        email: email,
        phone: generatePhoneNumber(),
        password: hashedPassword,
        avatar: generateAvatarURL(avatarIndex++),
        shop_id: shopId, // Link employee to shop
        status: 'ACTIVE',
        email_verified: true,
        phone_verified: true,
        created_at: now,
        updated_at: now,
        role: 'STAFF' // All shop employees have STAFF role in authentication
      };

      employeeUsers.push(employeeUser);
      registry.register('users', userId, employeeUser);

      // Generate salary
      const baseSalary = Math.floor(
        Math.random() * (roleConfig.baseSalary[1] - roleConfig.baseSalary[0]) + roleConfig.baseSalary[0]
      );
      const commission = parseFloat(
        (Math.random() * (roleConfig.commission[1] - roleConfig.commission[0]) + roleConfig.commission[0]).toFixed(2)
      );

      // Create shop member record
      const shopMember: ShopMember = {
        shop_member_id: shopMemberId,
        shop_id: shopId,
        user_id: userId,
        role_name: roleConfig.name,
        department: roleConfig.department,
        join_date: generateJoinDate(),
        status: 'ACTIVE',
        end_date: null,
        work_schedule: WORK_SCHEDULES[Math.floor(Math.random() * WORK_SCHEDULES.length)],
        salary_type: 'MONTHLY',
        base_salary: baseSalary,
        commission: commission,
        created_at: now,
        updated_at: now
      };

      shopMembers.push(shopMember);
      registry.register('shop_members', shopMemberId, shopMember);
    }
  }

  return { shopMembers, employeeUsers };
}

/**
 * Validate shop member data
 */
export function validateShopMembers(shopMembers: ShopMember[]): boolean {
  for (const member of shopMembers) {
    if (!member.shop_id || !member.user_id || !member.role_name) {
      return false;
    }
    if (member.base_salary <= 0) {
      return false;
    }
    if (member.commission < 0 || member.commission > 1) {
      return false;
    }
  }
  return true;
}

/**
 * Get employee credentials for documentation
 */
export function getEmployeeCredentials(employeeUsers: ShopEmployee[]): Array<{
  email: string;
  password: string;
  role: string;
  shop_id: string | null;
}> {
  return employeeUsers.map(emp => ({
    email: emp.email,
    password: 'Employee123!',
    role: emp.role,
    shop_id: emp.shop_id
  }));
}
