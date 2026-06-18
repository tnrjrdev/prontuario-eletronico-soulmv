import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { DataTable, type Column } from './DataTable'

interface Linha { id: number; nome: string }

const dados: Linha[] = [
  { id: 1, nome: 'Bruno' },
  { id: 2, nome: 'Ana' },
]

const colunas: Column<Linha>[] = [
  { key: 'nome', header: 'Nome', sortable: true, sortAccessor: (r) => r.nome, render: (r) => r.nome },
]

describe('DataTable', () => {
  it('renderiza as linhas de dados', () => {
    render(<DataTable data={dados} columns={colunas} rowKey={(r) => r.id} />)
    expect(screen.getByText('Bruno')).toBeInTheDocument()
    expect(screen.getByText('Ana')).toBeInTheDocument()
  })

  it('filtra pela busca instantânea', async () => {
    render(<DataTable data={dados} columns={colunas} rowKey={(r) => r.id} searchAccessor={(r) => r.nome} />)
    await userEvent.type(screen.getByPlaceholderText(/buscar/i), 'ana')
    expect(screen.getByText('Ana')).toBeInTheDocument()
    expect(screen.queryByText('Bruno')).not.toBeInTheDocument()
  })

  it('exibe estado vazio quando não há dados', () => {
    render(<DataTable data={[]} columns={colunas} rowKey={(r) => r.id} empty="Nada aqui" />)
    expect(screen.getByText('Nada aqui')).toBeInTheDocument()
  })

  it('ordena ao clicar no cabeçalho ordenável', async () => {
    render(<DataTable data={dados} columns={colunas} rowKey={(r) => r.id} />)
    await userEvent.click(screen.getByRole('button', { name: /nome/i }))
    const celulas = screen.getAllByRole('cell')
    expect(celulas[0]).toHaveTextContent('Ana') // ascendente: Ana antes de Bruno
  })
})
