package com.br.thalesdev.front_gestao_vagas.modules.candidate.controllers;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.thalesdev.front_gestao_vagas.modules.candidate.dto.CreateCandidateDTO;
import com.br.thalesdev.front_gestao_vagas.modules.candidate.services.ApplyJobService;
import com.br.thalesdev.front_gestao_vagas.modules.candidate.services.LoginCandidateService;
import com.br.thalesdev.front_gestao_vagas.modules.candidate.services.CreateCandidateService;
import com.br.thalesdev.front_gestao_vagas.modules.candidate.services.FindJobsService;
import com.br.thalesdev.front_gestao_vagas.modules.candidate.services.ProfileCandidateService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final LoginCandidateService loginCandidateService;
    private final ProfileCandidateService profileCandidateService;
    private final FindJobsService findJobsService;
    private final ApplyJobService applyJobService;
    private final CreateCandidateService createCandidateService;

    @GetMapping("/login")
    public String login() {
        return "candidate/login";
    }

    @PostMapping("/signIn")
    public String signIn(RedirectAttributes redirectAttributes, HttpSession session, String username, String password) {

        try {
            var token = this.loginCandidateService.execute(username, password);
            var grants = token.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase())).toList();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null, grants);
            auth.setDetails(token.getAccess_token());

            SecurityContextHolder.getContext().setAuthentication(auth);
            SecurityContext context = SecurityContextHolder.getContext();
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            session.setAttribute("token", token);

            return "redirect:/candidate/jobs";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("error_message", "Invalid username or password");
            return "redirect:/candidate/login";
        }

    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String profile(Model model) {

        try {
            var user = this.profileCandidateService.execute(getToken());
            model.addAttribute("user", user);

            return "candidate/profile";
        } catch (HttpClientErrorException e) {
            return "redirect:/candidate/login";
        }
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String jobs(Model model, String filter) {
        try {
            var jobs = this.findJobsService.execute(getToken(), filter);
            model.addAttribute("jobs", jobs);
        } catch (HttpClientErrorException e) {
            return "redirect:/candidate/login";
        }

        return "candidate/jobs";
    }

    @PostMapping("/jobs/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String applyJob(@RequestParam("jobId") UUID jobId) {

        this.applyJobService.execute(getToken(), jobId);

        return "redirect:/candidate/jobs";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("candidate", new CreateCandidateDTO());
        return "candidate/create";
    }

    @PostMapping("/create")
    public String save(RedirectAttributes redirectAttributes, CreateCandidateDTO candidate, Model model) {
        try {
            this.createCandidateService.execute(candidate);
            redirectAttributes.addFlashAttribute("success_message", "Conta criada com sucesso!");
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("error_message", e.getResponseBodyAsString());
        }

        model.addAttribute("candidate", candidate);
        return "redirect:/candidate/create";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContext context = SecurityContextHolder.getContext();
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        session.setAttribute("token", null);

        return "redirect:/home/";
    }

    private String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getDetails().toString();
    }

}
