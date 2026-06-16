import { useMemo, useState } from 'react'
import { Trash2, AlertTriangle, ShieldAlert, Info } from 'lucide-react'
import { Modal } from './Modal'
import { AsyncCombobox } from './AsyncCombobox'
import { Button, Input, Select, Alert } from './ui'
import { useToast } from './Toast'
import { useCriarPrescricao } from '../hooks/usePep'
import { medicamentoService } from '../services/catalogos.service'
import { extractError } from '../services/api'
import { VIA_ADMINISTRACAO_LABELS } from '../utils/constants'
import { analisarMedicamento, tokensDeAlergia, temAlertaCritico, type CdsAlerta } from '../lib/cds'
import { cn } from '../utils/cn'
import type { ItemPrescricao, Medicamento, ViaAdministracao } from '../types'

interface ItemStaged {
  med: Medicamento
  dose: string
  via: ViaAdministracao
  frequencia: string
  duracao: string
}

const VIAS = Object.keys(VIA_ADMINISTRACAO_LABELS) as ViaAdministracao[]

export function NovaPrescricaoModal({
  atendimentoId, alergias, itensAtivos, onClose,
}: {
  atendimentoId: number
  alergias?: string | null
  itensAtivos: ItemPrescricao[]
  onClose: () => void
}) {
  const toast = useToast()
  const criar = useCriarPrescricao(atendimentoId)

  const [observacao, setObservacao] = useState('')
  const [itens, setItens] = useState<ItemStaged[]>([])
  const [ciente, setCiente] = useState(false)

  const alergiaTokens = useMemo(() => tokensDeAlergia(alergias), [alergias])
  const nomesAtivosBase = useMemo(() => itensAtivos.map((i) => i.medicamentoNome), [itensAtivos])

  // Alertas por item (inclui duplicidade contra itens ativos e contra os já adicionados).
  const alertasPorItem = useMemo(() => {
    return itens.map((it, idx) => {
      const nomesAtivos = [...nomesAtivosBase, ...itens.filter((_, i) => i !== idx).map((o) => o.med.nome)]
      return analisarMedicamento(
        { nome: it.med.nome, principioAtivo: it.med.principioAtivo, controlado: it.med.controlado },
        { alergiaTokens, nomesAtivos }
      )
    })
  }, [itens, nomesAtivosBase, alergiaTokens])

  const haCritico = alertasPorItem.some((a) => temAlertaCritico(a))
  const podeSalvar = itens.length > 0 && itens.every((i) => i.dose.trim()) && (!haCritico || ciente)

  const adicionar = (med: Medicamento | null) => {
    if (!med) return
    setItens((cur) => [...cur, { med, dose: '', via: 'ORAL', frequencia: '', duracao: '' }])
  }
  const remover = (idx: number) => setItens((cur) => cur.filter((_, i) => i !== idx))
  const editar = (idx: number, campo: keyof ItemStaged, valor: string) =>
    setItens((cur) => cur.map((it, i) => (i === idx ? { ...it, [campo]: valor } : it)))

  const salvar = async () => {
    try {
      await criar.mutateAsync({
        observacao: observacao.trim() || undefined,
        itens: itens.map((it) => ({
          medicamentoId: it.med.id,
          dose: it.dose.trim(),
          via: it.via,
          frequencia: it.frequencia.trim() || undefined,
          duracao: it.duracao.trim() || undefined,
        })),
      })
      toast.success('Prescrição registrada.')
      onClose()
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  return (
    <Modal
      open
      onClose={onClose}
      size="xl"
      title="Nova prescrição"
      description="Adicione os medicamentos. Os alertas de segurança são verificados automaticamente."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancelar</Button>
          <Button onClick={salvar} disabled={!podeSalvar || criar.isPending}>
            {criar.isPending ? 'Salvando…' : 'Assinar e prescrever'}
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        {alergias && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <div><strong>Alergias do paciente:</strong> {alergias}</div>
          </Alert>
        )}

        <AsyncCombobox
          label="Adicionar medicamento"
          placeholder="Buscar medicamento por nome ou princípio ativo…"
          fetcher={(q) => medicamentoService.buscar(q)}
          getValue={(m) => m.id}
          getLabel={(m) => m.nome}
          getHint={(m) => [m.principioAtivo, m.concentracao].filter(Boolean).join(' • ') || undefined}
          onSelect={adicionar}
        />

        {itens.length === 0 ? (
          <p className="rounded-lg border border-dashed border-border py-8 text-center text-sm text-muted-foreground">
            Nenhum item adicionado ainda.
          </p>
        ) : (
          <div className="space-y-3">
            {itens.map((it, idx) => {
              const alertas = alertasPorItem[idx]
              return (
                <div key={`${it.med.id}-${idx}`} className={cn('rounded-lg border p-3', temAlertaCritico(alertas) ? 'border-destructive/40 bg-destructive/5' : 'border-border')}>
                  <div className="mb-2 flex items-start justify-between gap-2">
                    <div className="font-semibold text-foreground">
                      {it.med.nome}
                      {it.med.concentracao && <span className="ml-1 text-sm font-normal text-muted-foreground">{it.med.concentracao}</span>}
                    </div>
                    <button type="button" onClick={() => remover(idx)} className="rounded p-1 text-muted-foreground hover:bg-accent hover:text-destructive" aria-label="Remover item"><Trash2 className="h-4 w-4" /></button>
                  </div>

                  {alertas.length > 0 && (
                    <ul className="mb-3 space-y-1">
                      {alertas.map((a, i) => <CdsLinha key={i} alerta={a} />)}
                    </ul>
                  )}

                  <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                    <Input label="Dose" placeholder="ex.: 500 mg" value={it.dose} onChange={(e) => editar(idx, 'dose', e.target.value)} required />
                    <Select label="Via" value={it.via} onChange={(e) => editar(idx, 'via', e.target.value)}>
                      {VIAS.map((v) => <option key={v} value={v}>{VIA_ADMINISTRACAO_LABELS[v]}</option>)}
                    </Select>
                    <Input label="Frequência" placeholder="ex.: 8/8h" value={it.frequencia} onChange={(e) => editar(idx, 'frequencia', e.target.value)} />
                    <Input label="Duração" placeholder="ex.: 7 dias" value={it.duracao} onChange={(e) => editar(idx, 'duracao', e.target.value)} />
                  </div>
                </div>
              )
            })}
          </div>
        )}

        <div>
          <label className="mb-1.5 block text-sm font-medium">Observações da prescrição</label>
          <textarea
            value={observacao}
            onChange={(e) => setObservacao(e.target.value)}
            rows={2}
            className="w-full rounded-md border border-input bg-background p-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
            placeholder="Orientações gerais (opcional)"
          />
        </div>

        {haCritico && (
          <label className="flex items-start gap-2 rounded-lg border border-destructive/40 bg-destructive/5 p-3 text-sm">
            <input type="checkbox" checked={ciente} onChange={(e) => setCiente(e.target.checked)} className="mt-0.5 h-4 w-4 rounded border-input text-destructive focus:ring-destructive" />
            <span className="font-medium text-destructive">Estou ciente do(s) alerta(s) crítico(s) e assumo a responsabilidade clínica por prescrever mesmo assim.</span>
          </label>
        )}
      </div>
    </Modal>
  )
}

function CdsLinha({ alerta }: { alerta: CdsAlerta }) {
  const critico = alerta.severidade === 'critico'
  const Icon = alerta.tipo === 'ALERGIA' ? ShieldAlert : alerta.tipo === 'DUPLICIDADE' ? Info : AlertTriangle
  return (
    <li className={cn('flex items-center gap-2 rounded-md px-2 py-1 text-xs font-medium', critico ? 'bg-destructive/10 text-destructive' : 'bg-warning/10 text-warning')}>
      <Icon className="h-3.5 w-3.5 shrink-0" />
      {alerta.mensagem}
    </li>
  )
}
