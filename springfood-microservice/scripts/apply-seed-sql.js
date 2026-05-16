/**
 * Apply seed-product-images.sql to Neon production directly via pg client.
 * Wraps everything in a transaction so failure rolls back.
 *
 * Run: node apply-seed-sql.js
 */
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

// Build connection string from JDBC-style env (strip the jdbc: prefix, append credentials)
function buildConnString() {
  // DEFAULT_DATABASE_URL = jdbc:postgresql://host/db?sslmode=require
  const jdbc = process.env.DEFAULT_DATABASE_URL;
  const u = process.env.DEFAULT_DATABASE_USERNAME;
  const p = process.env.DEFAULT_DATABASE_PASSWORD;
  if (!jdbc || !u || !p) throw new Error('Missing DB env vars in .env');
  const noJdbc = jdbc.replace(/^jdbc:/, '');
  // postgresql://host/db?sslmode=require -> postgresql://user:pass@host/db?sslmode=require
  return noJdbc.replace('postgresql://', `postgresql://${encodeURIComponent(u)}:${encodeURIComponent(p)}@`);
}

async function main() {
  const sqlFile = path.join(__dirname, 'seed-product-images.sql');
  const sql = fs.readFileSync(sqlFile, 'utf8');

  const updateCount = (sql.match(/^UPDATE/gm) || []).length;
  console.log(`SQL file: ${sqlFile}`);
  console.log(`UPDATE statements: ${updateCount}`);

  const conn = buildConnString();
  console.log(`DB host: ${new URL(conn).host}`);

  const client = new Client({ connectionString: conn });
  await client.connect();
  console.log('[OK] Connected\n');

  try {
    // The SQL file already contains BEGIN; ... COMMIT;
    const t0 = Date.now();
    const res = await client.query(sql);
    const dt = Date.now() - t0;
    console.log(`[OK] Executed in ${dt}ms`);

    // pg returns array of results when multiple statements; report row counts
    const results = Array.isArray(res) ? res : [res];
    const totalAffected = results.reduce((s, r) => s + (r.rowCount || 0), 0);
    console.log(`[OK] Total rows affected: ${totalAffected}`);

    // Verification query
    const v = await client.query(`
      SELECT
        COUNT(*) FILTER (WHERE images IS NULL) AS null_images,
        COUNT(*) FILTER (WHERE images = '[]'::jsonb) AS empty_images,
        COUNT(*) FILTER (WHERE jsonb_array_length(images) BETWEEN 1 AND 3) AS ok_images,
        COUNT(*) FILTER (WHERE images::text LIKE '%placeholder%') AS still_placeholder,
        COUNT(*) AS total
      FROM springfood_product.products;
    `);
    console.log('\nVerification:');
    console.table(v.rows);

    const sample = await client.query(`
      SELECT name, images
      FROM springfood_product.products
      WHERE name IN ('Phở Bò Tái', 'Bún Chả', 'Cà Phê Sữa Đá', 'Trà Sữa Truyền Thống', 'Pizza Hải Sản')
      ORDER BY name;
    `);
    console.log('\nSample 5 products:');
    sample.rows.forEach(r => {
      console.log(`  ${r.name}:`);
      r.images.forEach(u => console.log(`    ${u}`));
    });
  } catch (e) {
    console.error('\n[FAIL]', e.message);
    if (e.position) console.error('SQL position:', e.position);
    process.exitCode = 1;
  } finally {
    await client.end();
  }
}

main();
