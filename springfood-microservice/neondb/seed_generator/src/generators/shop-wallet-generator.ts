/**
 * Shop Wallet Generator Module
 * 
 * Generates one wallet per shop with realistic balance data.
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';

export interface ShopWallet {
  wallet_id: string;
  shop_id: string;
  balance: number;
  pending_amount: number;
  locked_amount: number;
  created_at: Date;
  updated_at: Date;
}

/**
 * Generate shop wallets (one per shop)
 * 
 * @param registry - ID registry containing shop IDs
 * @returns Array of shop wallet records
 */
export function generateShopWallets(registry: IDRegistry): ShopWallet[] {
  const now = new Date();
  const shopWallets: ShopWallet[] = [];
  const shopIds = registry.getAllIds('shops');
  
  if (shopIds.length === 0) {
    throw new Error('No shops found in registry. Generate shops first.');
  }

  for (const shopId of shopIds) {
    const walletId = generateUUID();
    
    // Generate realistic wallet amounts (in VND)
    // Balance: 0 - 50,000,000 VND (0 - 50M)
    const balance = parseFloat((Math.random() * 50000000).toFixed(2));
    
    // Pending amount: 0 - 5,000,000 VND (0 - 5M)
    const pendingAmount = parseFloat((Math.random() * 5000000).toFixed(2));
    
    // Locked amount: 0 - 1,000,000 VND (0 - 1M)
    const lockedAmount = parseFloat((Math.random() * 1000000).toFixed(2));

    const shopWallet: ShopWallet = {
      wallet_id: walletId,
      shop_id: shopId,
      balance: balance,
      pending_amount: pendingAmount,
      locked_amount: lockedAmount,
      created_at: now,
      updated_at: now
    };

    shopWallets.push(shopWallet);
    registry.register('shop_wallets', walletId, shopWallet);
  }

  return shopWallets;
}

/**
 * Validate shop wallet data
 */
export function validateShopWallets(shopWallets: ShopWallet[]): boolean {
  for (const wallet of shopWallets) {
    if (!wallet.wallet_id || !wallet.shop_id) {
      return false;
    }
    if (wallet.balance < 0 || wallet.pending_amount < 0 || wallet.locked_amount < 0) {
      return false;
    }
  }
  return true;
}

/**
 * Get total balance across all shop wallets
 */
export function getTotalBalance(shopWallets: ShopWallet[]): number {
  return shopWallets.reduce((sum, wallet) => sum + wallet.balance, 0);
}

/**
 * Get wallet by shop ID
 */
export function getWalletByShopId(shopWallets: ShopWallet[], shopId: string): ShopWallet | undefined {
  return shopWallets.find(w => w.shop_id === shopId);
}
