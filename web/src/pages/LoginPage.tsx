import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Alert, Button, Input } from '../components/ui'
import { extractError } from '../services/api'
import { Activity, ShieldCheck, HeartPulse } from 'lucide-react'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [loginValue, setLoginValue] = useState('admin')
  const [senha, setSenha] = useState('admin123')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setErro('')
    setCarregando(true)
    try {
      await login(loginValue, senha)
      navigate('/')
    } catch (err) {
      setErro(extractError(err))
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-background">
      {/* Left side - Branding */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between bg-primary p-12 text-primary-foreground relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/90 to-brand-700/90 z-0" />
        <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=2053&auto=format&fit=crop')] bg-cover bg-center mix-blend-overlay opacity-20 z-0" />
        
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-12">
            <Activity className="h-8 w-8 text-primary-foreground" />
            <span className="text-2xl font-bold tracking-tight">SOUL MV Hospitalar</span>
          </div>
          
          <div className="space-y-6 max-w-lg mt-24">
            <h1 className="text-4xl font-bold tracking-tight leading-tight">
              A próxima geração do <br/>Prontuário Eletrônico
            </h1>
            <p className="text-lg text-primary-foreground/80 leading-relaxed">
              Design centrado no profissional de saúde. Menos cliques, mais tempo para o que importa: o cuidado com o paciente.
            </p>
          </div>
        </div>

        <div className="relative z-10 flex gap-8 text-sm text-primary-foreground/70">
          <div className="flex items-center gap-2"><ShieldCheck className="h-4 w-4"/> Segurança LGPD</div>
          <div className="flex items-center gap-2"><HeartPulse className="h-4 w-4"/> Foco Clínico</div>
        </div>
      </div>

      {/* Right side - Login Form */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-8">
        <div className="w-full max-w-md space-y-8">
          <div className="text-center lg:text-left space-y-2">
            <div className="flex items-center justify-center lg:hidden gap-2 mb-8 text-primary">
              <Activity className="h-8 w-8" />
              <span className="text-2xl font-bold tracking-tight">SOUL MV</span>
            </div>
            <h2 className="text-3xl font-bold tracking-tight text-foreground">Bem-vindo(a)</h2>
            <p className="text-muted-foreground">Acesse o sistema com suas credenciais</p>
          </div>

          <form onSubmit={onSubmit} className="space-y-6">
            {erro && <Alert variant="destructive">{erro}</Alert>}
            
            <div className="space-y-4">
              <Input 
                label="Nome de usuário" 
                placeholder="Ex: dr.joao"
                value={loginValue} 
                onChange={(e) => setLoginValue(e.target.value)} 
                autoFocus 
              />
              <div className="space-y-1">
                <Input
                  label="Senha"
                  type="password"
                  placeholder="••••••••"
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                />
                <div className="text-right">
                  <a href="#" className="text-sm font-medium text-primary hover:underline">Esqueceu a senha?</a>
                </div>
              </div>
            </div>

            <Button type="submit" className="w-full h-11 text-base" disabled={carregando}>
              {carregando ? 'Autenticando...' : 'Entrar no sistema'}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground pt-6">
            Dúvidas ou problemas de acesso? <br/>
            <a href="#" className="text-primary hover:underline">Contate o suporte de TI</a>
          </p>
        </div>
      </div>
    </div>
  )
}
