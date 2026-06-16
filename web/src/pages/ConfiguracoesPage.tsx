import { useState } from 'react'
import { Settings, Building2, ShieldPlus, Pill, ClipboardList, Stethoscope } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { Tabs, type TabItem } from '../components/Tabs'
import { Badge } from '../components/ui'
import { CatalogTab, type FormValues } from '../components/CatalogTab'
import {
  setorService, convenioService, medicamentoService, procedimentoService, cid10Service,
} from '../services/catalogos.service'
import { TIPO_SETOR_LABELS, TIPO_CONVENIO_LABELS } from '../utils/constants'
import { formatMoney } from '../utils/format'
import type { Cid10, Convenio, Medicamento, ProcedimentoTuss, Setor, TipoConvenio, TipoSetor } from '../types'

type TabKey = 'setores' | 'convenios' | 'medicamentos' | 'procedimentos' | 'cid10'

const TABS: TabItem<TabKey>[] = [
  { key: 'setores', label: 'Setores', icon: Building2 },
  { key: 'convenios', label: 'Convênios', icon: ShieldPlus },
  { key: 'medicamentos', label: 'Medicamentos', icon: Pill },
  { key: 'procedimentos', label: 'Procedimentos TUSS', icon: ClipboardList },
  { key: 'cid10', label: 'CID-10', icon: Stethoscope },
]

const tipoSetorOpts = Object.entries(TIPO_SETOR_LABELS).map(([value, label]) => ({ value, label }))
const tipoConvenioOpts = Object.entries(TIPO_CONVENIO_LABELS).map(([value, label]) => ({ value, label }))

const str = (v: FormValues[string]) => String(v ?? '').trim()
const opt = (v: FormValues[string]) => { const s = str(v); return s ? s : undefined }

export function ConfiguracoesPage() {
  const [tab, setTab] = useState<TabKey>('setores')

  return (
    <div>
      <PageHeader
        title="Configurações"
        subtitle="Catálogos e parâmetros do sistema"
        icon={<Settings />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Configurações' }]}
      />

      <div className="mb-5">
        <Tabs tabs={TABS} value={tab} onChange={setTab} />
      </div>

      {tab === 'setores' && (
        <CatalogTab<Setor>
          queryKey="cfg-setores"
          itemLabel="Setor"
          fetchAll={setorService.listar}
          searchAccessor={(s) => `${s.nome} ${TIPO_SETOR_LABELS[s.tipo] ?? ''}`}
          baseColumns={[
            { key: 'nome', header: 'Nome', sortable: true, sortAccessor: (s) => s.nome, render: (s) => <span className="font-medium text-foreground">{s.nome}</span> },
            { key: 'tipo', header: 'Tipo', render: (s) => <Badge color="blue">{TIPO_SETOR_LABELS[s.tipo] ?? s.tipo}</Badge> },
            { key: 'descricao', header: 'Descrição', render: (s) => <span className="text-muted-foreground">{s.descricao || '—'}</span> },
          ]}
          fields={[
            { name: 'nome', label: 'Nome', required: true, full: true },
            { name: 'tipo', label: 'Tipo', type: 'select', options: tipoSetorOpts, required: true },
            { name: 'descricao', label: 'Descrição', full: true },
          ]}
          emptyForm={{ nome: '', tipo: '', descricao: '' }}
          toForm={(s) => ({ nome: s.nome, tipo: s.tipo, descricao: s.descricao ?? '' })}
          save={(id, f) => {
            const p = { nome: str(f.nome), tipo: str(f.tipo) as TipoSetor, descricao: opt(f.descricao) }
            return id ? setorService.atualizar(id, p) : setorService.criar(p)
          }}
          getAtivo={(s) => s.ativo}
          toggleStatus={(s, ativo) => setorService.atualizarStatus(s.id, ativo)}
        />
      )}

      {tab === 'convenios' && (
        <CatalogTab<Convenio>
          queryKey="cfg-convenios"
          itemLabel="Convênio"
          fetchAll={convenioService.listar}
          searchAccessor={(c) => `${c.nome} ${c.registroAns ?? ''}`}
          baseColumns={[
            { key: 'nome', header: 'Nome', sortable: true, sortAccessor: (c) => c.nome, render: (c) => <span className="font-medium text-foreground">{c.nome}</span> },
            { key: 'tipo', header: 'Tipo', render: (c) => <Badge color="blue">{TIPO_CONVENIO_LABELS[c.tipo] ?? c.tipo}</Badge> },
            { key: 'ans', header: 'Registro ANS', render: (c) => <span className="text-muted-foreground">{c.registroAns || '—'}</span> },
          ]}
          fields={[
            { name: 'nome', label: 'Nome', required: true, full: true },
            { name: 'tipo', label: 'Tipo', type: 'select', options: tipoConvenioOpts, required: true },
            { name: 'registroAns', label: 'Registro ANS' },
          ]}
          emptyForm={{ nome: '', tipo: '', registroAns: '' }}
          toForm={(c) => ({ nome: c.nome, tipo: c.tipo, registroAns: c.registroAns ?? '' })}
          save={(id, f) => {
            const p = { nome: str(f.nome), tipo: str(f.tipo) as TipoConvenio, registroAns: opt(f.registroAns) }
            return id ? convenioService.atualizar(id, p) : convenioService.criar(p)
          }}
          getAtivo={(c) => c.ativo}
          toggleStatus={(c, ativo) => convenioService.atualizarStatus(c.id, ativo)}
        />
      )}

      {tab === 'medicamentos' && (
        <CatalogTab<Medicamento>
          queryKey="cfg-medicamentos"
          itemLabel="Medicamento"
          fetchAll={medicamentoService.listar}
          searchAccessor={(m) => `${m.nome} ${m.principioAtivo ?? ''}`}
          baseColumns={[
            { key: 'nome', header: 'Nome', sortable: true, sortAccessor: (m) => m.nome, render: (m) => <span className="font-medium text-foreground">{m.nome}</span> },
            { key: 'principio', header: 'Princípio ativo', render: (m) => <span className="text-muted-foreground">{m.principioAtivo || '—'}</span> },
            { key: 'concentracao', header: 'Concentração', render: (m) => m.concentracao || '—' },
            { key: 'controlado', header: 'Controlado', render: (m) => m.controlado ? <Badge color="critical">Controlado</Badge> : <span className="text-muted-foreground">—</span> },
          ]}
          fields={[
            { name: 'nome', label: 'Nome', required: true, full: true },
            { name: 'principioAtivo', label: 'Princípio ativo' },
            { name: 'concentracao', label: 'Concentração', placeholder: 'ex.: 500 mg' },
            { name: 'controlado', label: 'Medicamento controlado', type: 'checkbox', full: true },
          ]}
          emptyForm={{ nome: '', principioAtivo: '', concentracao: '', controlado: false }}
          toForm={(m) => ({ nome: m.nome, principioAtivo: m.principioAtivo ?? '', concentracao: m.concentracao ?? '', controlado: m.controlado })}
          save={(id, f) => {
            const p = { nome: str(f.nome), principioAtivo: opt(f.principioAtivo), concentracao: opt(f.concentracao), controlado: Boolean(f.controlado) }
            return id ? medicamentoService.atualizar(id, p) : medicamentoService.criar(p)
          }}
          getAtivo={(m) => m.ativo}
          toggleStatus={(m, ativo) => medicamentoService.atualizarStatus(m.id, ativo)}
        />
      )}

      {tab === 'procedimentos' && (
        <CatalogTab<ProcedimentoTuss>
          queryKey="cfg-procedimentos"
          itemLabel="Procedimento"
          fetchAll={procedimentoService.listar}
          searchAccessor={(p) => `${p.codigoTuss} ${p.descricao}`}
          baseColumns={[
            { key: 'codigo', header: 'Código TUSS', sortable: true, sortAccessor: (p) => p.codigoTuss, render: (p) => <span className="font-mono font-medium text-foreground">{p.codigoTuss}</span> },
            { key: 'descricao', header: 'Descrição', render: (p) => p.descricao },
            { key: 'valor', header: 'Valor ref.', align: 'right', render: (p) => p.valorReferencia != null ? formatMoney(p.valorReferencia) : '—' },
          ]}
          fields={[
            { name: 'codigoTuss', label: 'Código TUSS', required: true },
            { name: 'valorReferencia', label: 'Valor de referência (R$)', type: 'number' },
            { name: 'descricao', label: 'Descrição', required: true, full: true },
          ]}
          emptyForm={{ codigoTuss: '', valorReferencia: '', descricao: '' }}
          toForm={(p) => ({ codigoTuss: p.codigoTuss, valorReferencia: p.valorReferencia != null ? String(p.valorReferencia) : '', descricao: p.descricao })}
          save={(id, f) => {
            const valor = str(f.valorReferencia)
            const p = { codigoTuss: str(f.codigoTuss), descricao: str(f.descricao), valorReferencia: valor ? Number(valor) : undefined }
            return id ? procedimentoService.atualizar(id, p) : procedimentoService.criar(p)
          }}
          getAtivo={(p) => p.ativo}
          toggleStatus={(p, ativo) => procedimentoService.atualizarStatus(p.id, ativo)}
        />
      )}

      {tab === 'cid10' && (
        <CatalogTab<Cid10>
          queryKey="cfg-cid10"
          itemLabel="CID-10"
          fetchAll={cid10Service.listar}
          searchAccessor={(c) => `${c.codigo} ${c.descricao}`}
          baseColumns={[
            { key: 'codigo', header: 'Código', sortable: true, sortAccessor: (c) => c.codigo, render: (c) => <span className="font-mono font-medium text-foreground">{c.codigo}</span> },
            { key: 'descricao', header: 'Descrição', render: (c) => c.descricao },
          ]}
          fields={[
            { name: 'codigo', label: 'Código', required: true, placeholder: 'ex.: J18.9' },
            { name: 'descricao', label: 'Descrição', required: true, full: true },
          ]}
          emptyForm={{ codigo: '', descricao: '' }}
          toForm={(c) => ({ codigo: c.codigo, descricao: c.descricao })}
          save={(id, f) => {
            const p = { codigo: str(f.codigo), descricao: str(f.descricao) }
            return id ? cid10Service.atualizar(id, p) : cid10Service.criar(p)
          }}
          remove={(c) => cid10Service.excluir(c.id)}
        />
      )}
    </div>
  )
}
