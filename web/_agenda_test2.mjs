import { chromium } from 'playwright';

const browser = await chromium.launch();
const page = await browser.newPage();

await page.goto('http://localhost:5173/login');
await page.getByPlaceholder('Ex: dr.joao').fill('admin');
await page.getByPlaceholder('••••••••').fill('admin123');
await page.getByRole('button', { name: 'Entrar no sistema' }).click();
await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 });

await page.goto('http://localhost:5173/agenda');
await page.getByRole('button', { name: 'Nova marcação' }).click();
await page.waitForSelector('text=Reserve um horário na agenda do profissional.');

// Reproduz exatamente o cenário do usuário: digita "tary" no paciente (sem selecionar,
// pois não existe paciente com esse nome), preenche profissional/setor/data, e salva.
const combo = page.getByPlaceholder('Buscar paciente por nome…');
await combo.click();
await combo.fill('tary');
await page.waitForTimeout(900);

const profissionalSelect = page.locator('select').filter({ hasText: 'Selecione' }).first();
await page.getByRole('combobox').first().selectOption({ label: 'Dr. Marcos Medico' }).catch(() => {});

await page.screenshot({ path: 'C:\\Users\\taryj\\AppData\\Local\\Temp\\claude\\f--Dev-Prontuario-eletronico\\dbb2afb4-b835-491d-a165-d799ae2c4610\\scratchpad\\agenda_tary_sem_match.png' });

await page.getByRole('button', { name: 'Salvar' }).click();
await page.waitForTimeout(500);

const bodyText = await page.locator('body').innerText();
console.log('Toast "Selecione um paciente" apareceu:', bodyText.includes('Selecione um paciente'));

await page.screenshot({ path: 'C:\\Users\\taryj\\AppData\\Local\\Temp\\claude\\f--Dev-Prontuario-eletronico\\dbb2afb4-b835-491d-a165-d799ae2c4610\\scratchpad\\agenda_toast_validacao.png' });

await browser.close();
