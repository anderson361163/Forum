package br.com.alura.forum.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.controller.dto.DetalharTopicoDto;
import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.form.AtualizacaoTopicoForm;
import br.com.alura.forum.form.TopicoForm;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;


//A anotação RestController implementa o @ResponseBody em todos os demais métodos
@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	//@RequestMapping(value="/topicos", method =  RequestMethod.GET)
	@GetMapping
	public List<TopicoDto> lista(String nomeCurso){
		/*vai criar uma lista com 3 elementos em formato JSON
		 * por debaixo dos panos, o Spring boot usa a biblioteca Jackson
		 */
		if(nomeCurso == null) {
			List<Topico> topicos = topicoRepository.findAll();
			return TopicoDto.converter(topicos);
		}else {
//			List<Topico> topicos = topicoRepository.findByCurso_Nome(nomeCurso);
			List<Topico> topicos = topicoRepository.carregarPorNomeDoCurso(nomeCurso);
			return TopicoDto.converter(topicos);
		}
		
	}
	
	/*
	 * O Valid pede para o controller aplicar as anotações do benvalidation
	 */
	@PostMapping
	@Transactional
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form,
			UriComponentsBuilder uriBuilder) {
		Topico topico = form.converter(cursoRepository);
		topicoRepository.save(topico);
		
		
		
		//--------------------------------------------------------------------
				/* O URIBuilder vai pegar o caminho básico e adicionar o 
				 * recurso que está dentro do path
				 **/
		URI uri = uriBuilder.path("/topicos/{id}")
				/*o BuildAndExpande serve para informar o que vai 
				 *ser colocar dentro de {id} enquanto o toURI converte tudo isso em URI
				*/
				.buildAndExpand(topico.getId()).toUri();
		/*PARA REPASSAR QUE O RECURSO POR INSERIDO NO SERVIDOR (MÉTODO 201)
		*se faz necessário enviar a URI da onde está o recurso e no corpo da requisição,
		* manda o tópico
		*/
		return ResponseEntity.created(uri).body(new TopicoDto(topico));
		
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<DetalharTopicoDto> detalhar(@PathVariable Long id) {
		Optional<Topico> topico = topicoRepository.findById(id);
		if(topico.isPresent()) {
			return ResponseEntity.ok(new DetalharTopicoDto(topico.get()));
		}
		return ResponseEntity.notFound().build(); 
	}
	
	
	@PutMapping("/{id}") //O MÉTODO PUT SOBREESCREVE O RECURSO POR COMPLETO
	@Transactional
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id,
							@RequestBody @Valid AtualizacaoTopicoForm form){
		Optional<Topico> optional = topicoRepository.findById(id);
		if(optional.isPresent()) {
			Topico topico = form.atualizar(id, topicoRepository);
			return ResponseEntity.ok(new TopicoDto(topico));
		}
		return ResponseEntity.notFound().build(); 
//		
		
		//ao contrário do método create no atualizar ele está dizendo apenas ok (passando o método 200)
		
	}
	
	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<?> remover(@PathVariable Long id){
		Optional<Topico> optional = topicoRepository.findById(id);
		if(optional.isPresent()) {
			topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.notFound().build(); 
	}
	
	
}
