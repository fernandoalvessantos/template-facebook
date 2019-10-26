package aas.exemplos;

import javax.websocket.server.PathParam;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class ContaController {

	private String client_id = ""; // ID client app fb
	private String client_scrt = ""; // pass client app fb


	@GetMapping("/conta/{id}")
	public ResponseEntity<Conta> recuperaConta(@PathVariable("id") Long id) {
		Conta existente = ContaRepository.verificaContas(id);
		if(existente == null){
			HttpHeaders headers = new HttpHeaders();

			headers.add("Location", "https://www.facebook.com/v4.0/dialog/oauth?" +
					"client_id="+client_id+"&" +
					"redirect_uri=http://localhost:8080/conta/cadastro/"+id+"&" +
					"state=codsegurancateste");

			return new ResponseEntity<Conta>(headers, HttpStatus. TEMPORARY_REDIRECT);
		}else{
			return ResponseEntity.status(HttpStatus.OK).body(existente);
		}
		//throw new UnsupportedOperationException("Endpoint01 não implementado");
		
		// Endpoint 01:
		// Verifica se existe alguma Conta com o id igual ao id recebido como parâmetro
		//
		// Se o identificador foi encontrado
		//     Retorna um JSON contendo os dados da conta;
		//     O status da resposta é um http 200;
		// 
		// Caso o identificador não seja encontrado
		//     O status da resposta é um http redirect 307;
		//     Adiciona na resposta um cabeçalho "Location", cujo valor é a página de login do facebook com os devidos parâmetros:
		//         cliente_id: o client_id da aplicação;
		//         redirect_uri: indica que o facebook deverá redirecionar a aplicação para o Endpoint 02, utilizando o id de entrada;
		//         state: uma string utilizada para garantir do remetente da mensagem.
	}
	
	@GetMapping("/conta/cadastro/{id}")
	public ResponseEntity<Conta> cadastrarConta(@PathVariable("id") Long id, @PathParam("code") String code, @PathParam("state") String state) {

		// Token do usuario
		RestTemplate restTemplate = new RestTemplate();

		String urlToken = "https://graph.facebook.com/v4.0/oauth/access_token?" +
				"client_id="+client_id+"" +
				"&redirect_uri=http://localhost:8080/conta/cadastro/"+id+"" +
				"&client_secret="+ client_scrt +"" +
				"&code="+code+"";
		RespostaToken tokenUsuario = restTemplate.getForObject(urlToken, RespostaToken.class);

		// buscando token de acesso
		String urlTokenAcesso = "https://graph.facebook.com/oauth/access_token?" +
				"client_id="+client_id+"&" +
				"client_secret="+ client_scrt +"&grant_type=client_credentials";

		RespostaToken tokenAcesso = restTemplate.getForObject(urlTokenAcesso, RespostaToken.class);

		//Validando
		String urlDebugToken = "https://graph.facebook.com/v4.0/debug_token?" +
				"input_token="+tokenUsuario.getAccess_token()+"&" +
				"access_token="+tokenAcesso.getAccess_token();
		ResponseDebugToken responseDebugToken = restTemplate.getForObject(urlDebugToken, ResponseDebugToken.class);

		//get Dados Usuario
		String urlDados = "https://graph.facebook.com/" + responseDebugToken.getData().getUser_id() +
				"?fields=id,name"+
				"&access_token="+tokenUsuario.getAccess_token();
		ResponseDadosUsuario responseDadosUsuario = restTemplate.getForObject(urlDados, ResponseDadosUsuario.class);

		Conta conta = new Conta();
		conta.setFacebookId(responseDadosUsuario.getId());
		conta.setNome(responseDadosUsuario.getName());
		conta.setId(id);
		ContaRepository.adicionaConta(conta);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", "http://localhost:8080/conta/"+id);

		return new ResponseEntity<Conta>(headers, HttpStatus. TEMPORARY_REDIRECT);
		//throw new UnsupportedOperationException("Endpoint02 não implementado");
		
		// Endpoint 02:
		// Realiza uma série de 3 chamadas para o servidor do facebook
		//     1) Envia o code para obter o token de acesso;
		//     2) Envia o token para ser auditado e obter id do usuário; 
		//     3) Obtem os dados da conta.
		// 
		// Salva os dados do usuário recebidos na terceira requisição
		//
		// Retorna um redirecionamento para o usuário:
		//     O status da resposta é um http redirect 307;
		//     Adiciona na resposta um cabeçalho "Location", cujo valor é o Endpoint 01 com o identificador do usuário
		//return ResponseEntity.status(HttpStatus.OK).body(conta);
	}
}
