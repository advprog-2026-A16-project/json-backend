package id.ac.ui.cs.advprog.jsonbackend.profile.dto;
import jakarta.validation.constraints.NotBlank;

public record KycRequest(
        @NotBlank(message = "Nama lengkap sesuai KTP wajib diisi") String fullName,
        @NotBlank(message = "Nomor Identitas (KTP) wajib diisi") String identityNumber,
        @NotBlank(message = "Tautan sosial media wajib diisi") String socialMediaLink
) {}