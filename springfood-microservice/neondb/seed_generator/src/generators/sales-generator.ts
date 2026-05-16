/**
 * Sales Generator Module
 * 
 * Generates 15-20 sales campaigns using sales templates.
 * Distributes sales across statuses: 35% active, 25% upcoming, 40% expired.
 * 
 * Requirements: 2.7
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { SALES_CAMPAIGNS, SALES_DISTRIBUTION, SaleCampaign } from '../templates/sales';

export interface Sale {
  sale_id: string;
  name: string;
  description: string;
  discount_percentage: number;
  start_date: Date;
  end_date: Date;
  conditions: string;
  created_at: Date;
  updated_at: Date;
  category: string; // TEMPORARY FIELD: Used for product assignment logic, must be removed before SQL generation
}

/**
 * Generate random number of days
 */
function randomDays(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Generate random number of hours
 */
function randomHours(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Get random campaign from templates
 */
function getRandomCampaign(): SaleCampaign {
  return SALES_CAMPAIGNS[Math.floor(Math.random() * SALES_CAMPAIGNS.length)];
}

/**
 * Generate 15-20 sales campaigns using sales templates
 * 
 * @param registry - ID registry to register sale IDs
 * @param count - Number of sales to generate (default: random 15-20)
 * @returns Array of sale records
 * 
 * Requirements:
 * - 2.7: Generate 15-20 sales campaigns using sales templates
 * - Distribute sales across statuses: 35% active, 25% upcoming, 40% expired
 * - Generate realistic date ranges based on status
 * - Set discount_percentage based on campaign category
 */
export function generateSales(registry: IDRegistry, count?: number): Sale[] {
  const now = new Date();
  const sales: Sale[] = [];
  
  // Determine total number of sales (15-20)
  const totalSales = count || (SALES_DISTRIBUTION.totalSales.min + 
    Math.floor(Math.random() * (SALES_DISTRIBUTION.totalSales.max - SALES_DISTRIBUTION.totalSales.min + 1)));
  
  // Calculate distribution
  const activeCount = Math.floor(totalSales * SALES_DISTRIBUTION.statusDistribution.active);
  const upcomingCount = Math.floor(totalSales * SALES_DISTRIBUTION.statusDistribution.upcoming);
  const expiredCount = totalSales - activeCount - upcomingCount;

  // Generate ACTIVE sales (currently running)
  for (let i = 0; i < activeCount; i++) {
    const campaign = getRandomCampaign();
    const saleId = generateUUID();
    
    // Start date: 2-7 days ago
    const daysAgo = randomDays(2, 7);
    const startDate = new Date(now.getTime() - daysAgo * 24 * 60 * 60 * 1000);
    
    // Calculate duration
    const durationMs = campaign.duration_hours 
      ? campaign.duration_hours * 60 * 60 * 1000 
      : (campaign.duration_days || 7) * 24 * 60 * 60 * 1000;
    
    // End date: start_date + duration (must be in future for active sales)
    const endDate = new Date(startDate.getTime() + durationMs);
    
    // If end date is in the past, adjust start date to make it active
    if (endDate < now) {
      const adjustedStartDate = new Date(now.getTime() - randomHours(1, 48) * 60 * 60 * 1000);
      const adjustedEndDate = new Date(adjustedStartDate.getTime() + durationMs);
      
      const sale: Sale = {
        sale_id: saleId,
        name: campaign.name,
        description: campaign.description,
        discount_percentage: campaign.discount_percentage,
        start_date: adjustedStartDate,
        end_date: adjustedEndDate,
        conditions: campaign.conditions,
        created_at: new Date(adjustedStartDate.getTime() - 24 * 60 * 60 * 1000), // Created 1 day before start
        updated_at: adjustedStartDate,
        category: campaign.category
      };
      
      sales.push(sale);
    } else {
      const sale: Sale = {
        sale_id: saleId,
        name: campaign.name,
        description: campaign.description,
        discount_percentage: campaign.discount_percentage,
        start_date: startDate,
        end_date: endDate,
        conditions: campaign.conditions,
        created_at: new Date(startDate.getTime() - 24 * 60 * 60 * 1000), // Created 1 day before start
        updated_at: startDate,
        category: campaign.category
      };
      
      sales.push(sale);
    }
    
    // Register sale ID in registry
    registry.register('sales', saleId, sales[sales.length - 1]);
  }

  // Generate UPCOMING sales (starting soon)
  for (let i = 0; i < upcomingCount; i++) {
    const campaign = getRandomCampaign();
    const saleId = generateUUID();
    
    // Start date: 1-7 days in future
    const daysAhead = randomDays(1, 7);
    const startDate = new Date(now.getTime() + daysAhead * 24 * 60 * 60 * 1000);
    
    // Calculate duration
    const durationMs = campaign.duration_hours 
      ? campaign.duration_hours * 60 * 60 * 1000 
      : (campaign.duration_days || 7) * 24 * 60 * 60 * 1000;
    
    // End date: start_date + duration
    const endDate = new Date(startDate.getTime() + durationMs);
    
    const sale: Sale = {
      sale_id: saleId,
      name: campaign.name,
      description: campaign.description,
      discount_percentage: campaign.discount_percentage,
      start_date: startDate,
      end_date: endDate,
      conditions: campaign.conditions,
      created_at: new Date(now.getTime() - randomDays(1, 3) * 24 * 60 * 60 * 1000), // Created 1-3 days ago
      updated_at: now,
      category: campaign.category
    };
    
    sales.push(sale);
    
    // Register sale ID in registry
    registry.register('sales', saleId, sale);
  }

  // Generate EXPIRED sales (for history)
  for (let i = 0; i < expiredCount; i++) {
    const campaign = getRandomCampaign();
    const saleId = generateUUID();
    
    // End date: 1-30 days ago
    const daysAgo = randomDays(1, 30);
    const endDate = new Date(now.getTime() - daysAgo * 24 * 60 * 60 * 1000);
    
    // Calculate duration
    const durationMs = campaign.duration_hours 
      ? campaign.duration_hours * 60 * 60 * 1000 
      : (campaign.duration_days || 7) * 24 * 60 * 60 * 1000;
    
    // Start date: end_date - duration
    const startDate = new Date(endDate.getTime() - durationMs);
    
    const sale: Sale = {
      sale_id: saleId,
      name: campaign.name,
      description: campaign.description,
      discount_percentage: campaign.discount_percentage,
      start_date: startDate,
      end_date: endDate,
      conditions: campaign.conditions,
      created_at: new Date(startDate.getTime() - 24 * 60 * 60 * 1000), // Created 1 day before start
      updated_at: endDate,
      category: campaign.category
    };
    
    sales.push(sale);
    
    // Register sale ID in registry
    registry.register('sales', saleId, sale);
  }

  return sales;
}

/**
 * Validate that 15-20 sales were generated
 */
export function validateSalesCount(sales: Sale[]): boolean {
  return sales.length >= SALES_DISTRIBUTION.totalSales.min && 
         sales.length <= SALES_DISTRIBUTION.totalSales.max;
}

/**
 * Validate sales status distribution
 * Should be approximately 35% active, 25% upcoming, 40% expired
 */
export function validateSalesStatusDistribution(sales: Sale[]): boolean {
  const now = new Date();
  
  const activeCount = sales.filter(s => s.start_date <= now && s.end_date > now).length;
  const upcomingCount = sales.filter(s => s.start_date > now).length;
  const expiredCount = sales.filter(s => s.end_date <= now).length;
  
  const total = sales.length;
  const activePercent = activeCount / total;
  const upcomingPercent = upcomingCount / total;
  const expiredPercent = expiredCount / total;
  
  // Allow 15% tolerance
  return (
    activePercent >= 0.20 && activePercent <= 0.50 &&
    upcomingPercent >= 0.10 && upcomingPercent <= 0.40 &&
    expiredPercent >= 0.25 && expiredPercent <= 0.55
  );
}

/**
 * Validate sales discount percentages are within valid ranges
 */
export function validateSalesDiscountRanges(sales: Sale[]): boolean {
  for (const sale of sales) {
    const category = sale.category;
    const range = SALES_DISTRIBUTION.discountRanges[category as keyof typeof SALES_DISTRIBUTION.discountRanges];
    
    if (!range) {
      console.error(`Unknown sale category: ${category}`);
      return false;
    }
    
    if (sale.discount_percentage < range.min || sale.discount_percentage > range.max) {
      console.error(`Sale ${sale.name} has discount ${sale.discount_percentage}% outside range ${range.min}-${range.max}%`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate sales date ranges are realistic for each status
 */
export function validateSalesDateRanges(sales: Sale[]): boolean {
  const now = new Date();
  
  for (const sale of sales) {
    // Validate start_date is before end_date
    if (sale.start_date >= sale.end_date) {
      console.error(`Sale ${sale.name} has invalid date range: start_date >= end_date`);
      return false;
    }
    
    // Validate created_at is before start_date
    if (sale.created_at > sale.start_date) {
      console.error(`Sale ${sale.name} has invalid created_at: created_at > start_date`);
      return false;
    }
    
    // Determine status
    const isActive = sale.start_date <= now && sale.end_date > now;
    const isUpcoming = sale.start_date > now;
    const isExpired = sale.end_date <= now;
    
    // Validate date ranges based on status
    if (isActive) {
      // Active sales should have started in the past and end in the future
      if (sale.start_date > now || sale.end_date <= now) {
        console.error(`Sale ${sale.name} marked as active but has invalid date range`);
        return false;
      }
    } else if (isUpcoming) {
      // Upcoming sales should start in the future
      if (sale.start_date <= now) {
        console.error(`Sale ${sale.name} marked as upcoming but has already started`);
        return false;
      }
    } else if (isExpired) {
      // Expired sales should have ended in the past
      if (sale.end_date > now) {
        console.error(`Sale ${sale.name} marked as expired but has not ended yet`);
        return false;
      }
    }
  }
  
  return true;
}

/**
 * Get sales by status
 */
export function getSalesByStatus(sales: Sale[]): {
  active: Sale[];
  upcoming: Sale[];
  expired: Sale[];
} {
  const now = new Date();
  
  return {
    active: sales.filter(s => s.start_date <= now && s.end_date > now),
    upcoming: sales.filter(s => s.start_date > now),
    expired: sales.filter(s => s.end_date <= now)
  };
}

/**
 * Get sales by category
 */
export function getSalesByCategory(sales: Sale[], category: string): Sale[] {
  return sales.filter(s => s.category === category);
}

/**
 * Get sales statistics
 */
export function getSalesStats(sales: Sale[]): {
  total: number;
  active: number;
  upcoming: number;
  expired: number;
  avgDiscount: number;
  minDiscount: number;
  maxDiscount: number;
} {
  const { active, upcoming, expired } = getSalesByStatus(sales);
  const discounts = sales.map(s => s.discount_percentage);
  
  return {
    total: sales.length,
    active: active.length,
    upcoming: upcoming.length,
    expired: expired.length,
    avgDiscount: discounts.reduce((sum, d) => sum + d, 0) / discounts.length,
    minDiscount: Math.min(...discounts),
    maxDiscount: Math.max(...discounts)
  };
}
